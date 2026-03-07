#!/bin/bash

# Цвета для вывода
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Функции для вывода
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Проверка наличия необходимых утилит
check_dependencies() {
    local deps=("javac" "cmake" "make" "g++")
    for dep in "${deps[@]}"; do
        if ! command -v "$dep" &> /dev/null; then
            print_error "$dep не найден. Установите его."
            exit 1
        fi
    done
    print_info "Все зависимости найдены"
}

# Создание директорий
create_dirs() {
    mkdir -p ./build
    mkdir -p ./out
    mkdir -p ./build
    mkdir -p implementation/src/main/resources/natives/libs
}

# Поиск Java файлов
find_java_files() {
    print_info "Поиск Java файлов..."
    
    local modules=("core" "implementation" "loaders" "generator" "examples" "effects" "profiler" "midi" "tools")
    
    for module in "${modules[@]}"; do
        if [ -d "${module}/src/main/java" ]; then
            find "${module}/src/main/java" -name "*.java" > "./build/${module}.txt"
            print_info "  Найдены файлы для модуля ${module}"
        else
            print_warn "  Директория ${module}/src/main/java не найдена"
            touch "./build/${module}.txt"
        fi
    done
}

# Компиляция Java модулей
compile_java_modules() {
    print_info "Компиляция Java модулей..."
    
    # Проверяем наличие cp.txt
    if [ ! -f "./build/cp.txt" ]; then
        print_warn "Файл classpath (cp.txt) не найден. Используется пустой classpath."
        local classpath=""
    else
        # shellcheck disable=SC2155
        local classpath=$(cat ./build/cp.txt)
    fi
    
    # Core - базовый модуль
    if [ -s ./build/core.txt ]; then
        print_info "  Компиляция core..."
        javac -d out/core -cp "$classpath" @./build/core.txt 2>&1 | tee ./build/core_compile.log
    fi
    
    # Loaders (зависит от core)
    if [ -s ./build/loaders.txt ]; then
        print_info "  Компиляция loaders..."
        javac -d out/loaders -cp "out/core:$classpath" @./build/loaders.txt 2>&1 | tee ./build/loaders_compile.log
    fi
    
    # Effects (зависит от core)
    if [ -s ./build/effects.txt ]; then
        print_info "  Компиляция effects..."
        javac -d out/effects -cp "out/core:$classpath" @./build/effects.txt 2>&1 | tee ./build/effects_compile.log
    fi
    
    # Profiler (зависит от core)
    if [ -s ./build/profiler.txt ]; then
        print_info "  Компиляция profiler..."
        javac -d out/profiler -cp "out/core:$classpath" @./build/profiler.txt 2>&1 | tee ./build/profiler_compile.log
    fi
    
    # MIDI (зависит от core)
    if [ -s ./build/midi.txt ]; then
        print_info "  Компиляция midi..."
        javac -d out/midi -cp "out/core:$classpath" @./build/midi.txt 2>&1 | tee ./build/midi_compile.log
    fi
    
    # Tools (зависит от core)
    if [ -s ./build/tools.txt ]; then
        print_info "  Компиляция tools..."
        javac -d out/tools -cp "out/core:$classpath" @./build/tools.txt 2>&1 | tee ./build/tools_compile.log
    fi

    # Generator (зависит от core)
    if [ -s ./build/generator.txt ]; then
        print_info "  Компиляция generator..."
        javac -d out/generator -cp "out/core:$classpath" @./build/generator.txt 2>&1 | tee ./build/generator_compile.log
    fi
    
    # Implementation (зависит от core, генерирует заголовки для JNI)
    if [ -s ./build/implementation.txt ]; then
        print_info "  Компиляция implementation..."
        javac -h ./implementation/src/main/cpp/mac/org/plovdev/audioengine \
              -d out/implementation \
              -cp "out/core:$classpath" \
              @./build/implementation.txt 2>&1 | tee ./build/implementation_compile.log
    fi
    
    # Examples (зависит от всех модулей)
    if [ -s ./build/examples.txt ]; then
        print_info "  Компиляция examples..."
        javac -d out/examples \
              -cp "out/implementation:out/generator:out/profiler:out/loaders:out/core:out/effects:out/midi:out/tools:$classpath" \
              @./build/examples.txt 2>&1 | tee ./build/examples_compile.log
    fi
}

# Поиск C++ файлов для CMake
find_cpp_files() {
    print_info "Поиск C++ файлов для CMake..."
    find . -name "*.cpp" > ./build/natives.txt
    # shellcheck disable=SC2155
    local count=$(wc -l < ./build/natives.txt)
    print_info "  Найдено $count C++ файлов"
}

# Компиляция нативной библиотеки с CMake
compile_native_cmake() {
    print_info "Компиляция нативной библиотеки с CMake..."
    
    # Определяем платформу
    # shellcheck disable=SC2155
    local os_name=$(uname -s)
    local jni_include_path=""
    local jni_include_darwin=""
    local jni_include_linux=""

    # Настройка путей для JNI в зависимости от платформы
    if [[ "$os_name" == "Darwin" ]]; then
        # macOS
        jni_include_path="/Library/Java/JavaVirtualMachines/jdk-18/Contents/Home/include"
        jni_include_darwin="$jni_include_path/darwin"
        print_info "  Платформа: macOS"
        print_info "  JNI include path: $jni_include_path"
        print_info "  JNI darwin include: $jni_include_darwin"
    elif [[ "$os_name" == "Linux" ]]; then
        # Linux
        jni_include_path="/usr/lib/jvm/java-18-openjdk/include"
        jni_include_linux="$jni_include_path/linux"
        print_info "  Платформа: Linux"
    else
        print_error "  Неподдерживаемая платформа: $os_name"
        exit 1
    fi

    # Проверяем существование путей JNI
    if [ ! -d "$jni_include_path" ]; then
        print_warn "  JNI include path не найден: $jni_include_path"
        print_warn "  Попытка найти JDK автоматически..."

        # Автопоиск JDK
        if command -v java &> /dev/null; then
            # shellcheck disable=SC2155
            local java_home=$(java -XshowSettings:properties -version 2>&1 | grep java.home | awk -F'=' '{print $2}' | tr -d ' ')
            jni_include_path="$java_home/include"

            if [[ "$os_name" == "Darwin" ]]; then
                jni_include_darwin="$jni_include_path/darwin"
            elif [[ "$os_name" == "Linux" ]]; then
                jni_include_linux="$jni_include_path/linux"
            fi

            print_info "  Найден JDK по пути: $java_home"
        fi
    fi

    # Дополнительная проверка для macOS
    if [[ "$os_name" == "Darwin" ]] && [ ! -d "$jni_include_darwin" ]; then
        print_error "  Не найден darwin JNI include: $jni_include_darwin"
        print_error "  Проверьте установку JDK"
        return 1
    fi

    # Переходим в директорию сборки
    cd ./build || exit

    # Конфигурация CMake
    print_info "  Конфигурация CMake..."

    # Для macOS передаем оба пути
    if [[ "$os_name" == "Darwin" ]]; then
        cmake ../implementation \
            -DJNI_INCLUDE_PATH="$jni_include_path" \
            -DJNI_INCLUDE_DARWIN="$jni_include_darwin" \
            2>&1 | tee ../build/cmake_configure.log
    else
        cmake ../implementation \
            -DJNI_INCLUDE_PATH="$jni_include_path" \
            -DJNI_INCLUDE_LINUX="$jni_include_linux" \
            2>&1 | tee ../build/cmake_configure.log
    fi

    # shellcheck disable=SC2181
    if [ $? -eq 0 ]; then
        print_info "    ✓ CMake сконфигурирован"
    else
        print_error "    ✗ Ошибка конфигурации CMake"
        cd ..
        return 1
    fi

    # Сборка
    print_info "  Сборка нативной библиотеки..."
    cmake --build . 2>&1 | tee ../build/cmake_build.log

    local build_result=$?

    if [ $build_result -eq 0 ]; then
        print_info "    ✓ Нативная библиотека собрана"

        # Проверяем, создалась ли библиотека
        if [[ "$os_name" == "Darwin" ]]; then
            if [ -f "../implementation/src/main/resources/natives/libs/audio-snd.dylib" ]; then
                print_info "    ✓ Библиотека создана: implementation/src/main/resources/natives/libs/audio-snd.dylib"
            else
                print_warn "    ! Библиотека не найдена в ожидаемом месте"
            fi
        elif [[ "$os_name" == "Linux" ]]; then
            if [ -f "../implementation/src/main/resources/natives/libs/audio-snd.so" ]; then
                print_info "    ✓ Библиотека создана: implementation/src/main/resources/natives/libs/audio-snd.so"
            fi
        fi
    else
        print_error "    ✗ Ошибка сборки нативной библиотеки"
        print_info "    Проверьте логи в builds/cmake_build.log"
    fi
    
    # Возвращаемся обратно
    cd ..
}

# Основная функция
main() {
    print_info "Начало сборки проекта..."
    
    check_dependencies
    create_dirs
    find_java_files
    find_cpp_files
    
    # Компиляция Java
    compile_java_modules
    
    # Компиляция нативной библиотеки с CMake
    compile_native_cmake
    
    # Если CMake не сработал, пробуем старый способ
    # shellcheck disable=SC2181
    if [ $? -ne 0 ]; then
        print_warn "CMake сборка не удалась..."
    fi
    
    print_info "Сборка завершена!"
    
    # Вывод итоговой информации
    echo ""
    echo "================== РЕЗУЛЬТАТЫ =================="
    echo "Java классы:"
    find ./out -name "*.class" | wc -l | xargs echo "  Количество:"
    
    echo ""
    echo "Нативные библиотеки:"
    if [[ "$(uname -s)" == "Darwin" ]]; then
        ls -la implementation/src/main/resources/natives/libs/*.dylib 2>/dev/null || echo "  Не найдены"
    elif [[ "$(uname -s)" == "Linux" ]]; then
        ls -la implementation/src/main/resources/natives/libs/*.so 2>/dev/null || echo "  Не найдены"
    fi
    echo "================================================"

    rm audio-snd.dylib;
    rm audio-snd.so;
    rm audio-snd.dll;
}

main "$@"
