find core/src/main/java -name "*.java" > ./builds/core.txt
find implementation/src/main/java -name "*.java" > ./builds/implementation.txt
find loaders/src/main/java -name "*.java" > ./builds/loaders.txt
find generator/src/main/java -name "*.java" > ./builds/generator.txt
find examples/src/main/java -name "*.java" > ./builds/examples.txt
find effects/src/main/java -name "*.java" > ./builds/effects.txt
find profiler/src/main/java -name "*.java" > ./builds/profiler.txt
find midi/src/main/java -name "*.java" > ./builds/midi.txt
find tools/src/main/java -name "*.java" > ./builds/tools.txt

find . -name "*.cpp" > ./builds/nativies.txt

# Компиляция core
javac -d out/core -cp $(cat ./builds/cp.txt) @./builds/core.txt

# Компиляция loaders
javac -d out/loaders -cp out/core:$(cat ./builds/cp.txt) @./builds/loaders.txt

# Компиляция effects
javac -d out/effects -cp out/core:$(cat ./builds/cp.txt) @./builds/effects.txt

# Компиляция profiler
javac -d out/profiler -cp out/core:$(cat ./builds/cp.txt) @./builds/profiler.txt

# Компиляция midi
javac -d out/midi -cp out/core:$(cat ./builds/cp.txt) @./builds/midi.txt

# Компиляция tools
javac -d out/tools -cp out/core:$(cat ./builds/cp.txt) @./builds/tools.txt

# Компиляция implementation
javac -h ./implementation/src/main/cpp/org/plovdev/audioengine -d out/implementation -cp out/core:$(cat ./builds/cp.txt) @./builds/implementation.txt

# Компиляция examples
javac -d out/examples -cp out/implementation:out/loaders:out/core:/out/effects:/out/profiler:/out/midi:$(cat ./builds/cp.txt) @./builds/examples.txt


# Компиляция нативной библиотеки
#g++ -I"/Library/Java/JavaVirtualMachines/jdk-18/Contents/Home/include" -I"/Library/Java/JavaVirtualMachines/jdk-18/Contents/Home/include/darwin" -dynamiclib -o implementation/libaudio-snd.dylib $(cat ./builds/nativies.txt) -framework AudioToolbox -framework CoreAudio -framework CoreFoundation -std=c++17
