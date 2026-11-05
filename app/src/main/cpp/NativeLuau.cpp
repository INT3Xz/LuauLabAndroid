#include <jni.h>
#include <string>
#include <sstream>
#include <vector>
#include <mutex>

#include "lua.h"
#include "lualib.h"
#include "Luau/Compiler.h"

namespace {
    std::string captured;
    int outputLimit = 48 * 1024;

    void appendOutput(const std::string& value) {
        if (captured.size() >= static_cast<size_t>(outputLimit)) return;
        size_t remaining = outputLimit - captured.size();
        captured.append(value.substr(0, remaining));
    }

    int labPrint(lua_State* L) {
        int count = lua_gettop(L);
        std::ostringstream line;
        for (int i = 1; i <= count; ++i) {
            if (i > 1) line << "\\t";
            size_t length = 0;
            const char* text = luaL_tolstring(L, i, &length);
            if (text) line.write(text, static_cast<std::streamsize>(length));
            lua_pop(L, 1);
        }
        line << "\\n";
        appendOutput(line.str());
        return 0;
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_example_luau_1lab_LuauEngine_execute(
        JNIEnv* env, jobject /* this */, jstring input) {
    captured.clear();

    if (!input) {
        return env->NewStringUTF("No source code was supplied.");
    }

    const char* chars = env->GetStringUTFChars(input, nullptr);
    if (!chars) return env->NewStringUTF("Could not read source code.");

    std::string source(chars);
    env->ReleaseStringUTFChars(input, chars);

    if (source.size() > 256 * 1024) {
        return env->NewStringUTF("Error: source is larger than 256 KB.");
    }

    lua_State* L = luaL_newstate();
    if (!L) return env->NewStringUTF("Error: could not create Luau VM.");

    // Open Luau's standard libraries, then remove facilities not needed
    // by this local teaching playground.
    luaL_openlibs(L);
    lua_pushcfunction(L, labPrint, "print");
    lua_setglobal(L, "print");

    const char* blocked[] = {
        "io", "os", "debug", "require", "package",
        "dofile", "loadfile", "loadstring"
    };
    for (const char* name : blocked) {
        lua_pushnil(L);
        lua_setglobal(L, name);
    }

    try {
        Luau::CompileOptions options;
        std::string bytecode = Luau::compile(source, options);
        int loadStatus = luau_load(L, "=LuauLab", bytecode.data(), bytecode.size(), 0);

        if (loadStatus != 0) {
            const char* message = lua_tostring(L, -1);
            appendOutput(std::string("Compile/load error: ") +
                         (message ? message : "unknown error") + "\\n");
        } else {
            int callStatus = lua_pcall(L, 0, LUA_MULTRET, 0);
            if (callStatus != 0) {
                const char* message = lua_tostring(L, -1);
                appendOutput(std::string("Runtime error: ") +
                             (message ? message : "unknown error") + "\\n");
            }
        }
    } catch (const std::exception& e) {
        appendOutput(std::string("Compiler exception: ") + e.what() + "\\n");
    } catch (...) {
        appendOutput("Unknown native exception.\\n");
    }

    if (captured.empty()) captured = "Execution finished. (No print output)";
    lua_close(L);
    return env->NewStringUTF(captured.c_str());
}
