cmake_minimum_required(VERSION 3.22.1)
project(luaulab)

include(FetchContent)
set(LUAU_BUILD_CLI OFF CACHE BOOL "" FORCE)
set(LUAU_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(LUAU_BUILD_WEB OFF CACHE BOOL "" FORCE)
FetchContent_Declare(
    Luau
    GIT_REPOSITORY https://github.com/luau-lang/luau.git
    GIT_TAG 0.650
)
FetchContent_MakeAvailable(Luau)

add_library(luaulab SHARED NativeLuau.cpp)
target_include_directories(luaulab PRIVATE
    ${luau_SOURCE_DIR}/VM/include
    ${luau_SOURCE_DIR}/Compiler/include
    ${luau_SOURCE_DIR}/Ast/include
    ${luau_SOURCE_DIR}/Common/include
)
find_library(log-lib log)

target_link_libraries(luaulab
    Luau.Compiler
    Luau.VM
    Luau.Ast
    Luau.Common
    ${log-lib}
)
