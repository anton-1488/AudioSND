find core/src/main/java -name "*.java" > ./builds/core.txt
find implementation/src/main/java -name "*.java" > ./builds/implementation.txt
find loaders/src/main/java -name "*.java" > ./builds/loaders.txt
find generator/src/main/java -name "*.java" > ./builds/generator.txt
find examples/src/main/java -name "*.java" > ./builds/examples.txt

find implementation/src/main/cpp/org/plovdev/audioengine -name "*.cpp" > ./builds/nativies.txt

# Компиляция core
javac -d out/core -cp $(cat ./builds/cp.txt) @./builds/core.txt

# Компиляция loaders
javac -d out/loaders -cp out/core:$(cat ./builds/cp.txt) @./builds/loaders.txt

# Компиляция implementation
javac -h ./implementation/src/main/cpp/org/plovdev/audioengine -d out/implementation -cp out/core:$(cat ./builds/cp.txt) @./builds/implementation.txt

# Компиляция examples
#javac -d out/examples -cp out/implementation:out/loaders:out/core$(cat cp.txt) @examples.txt


# Компиляция нативной библиотеки
g++ -I"/Library/Java/JavaVirtualMachines/jdk-18/Contents/Home/include" -I"/Library/Java/JavaVirtualMachines/jdk-18/Contents/Home/include/darwin" -dynamiclib -o implementation/libaudio-snd.dylib $(cat ./builds/nativies.txt) -framework AudioToolbox -framework CoreAudio -framework CoreFoundation -std=c++17
