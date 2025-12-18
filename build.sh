find core/src/main/java -name "*.java" > sources.txt

# Компиляция core
javac -d out/core -cp $(cat cp.txt) @sources.txt

# Компиляция implementation с зависимостью на core
javac -h ./implementation/src/main/cpp/org/plovdev/audioengine -d out/implementation -cp out/core:$(cat cp.txt) $(find implementation/src/main/java -name "*.java" ! -path "*test*" ! -path "Main.java")

g++ -I"/Library/Java/JavaVirtualMachines/jdk-18/Contents/Home/include" -I"/Library/Java/JavaVirtualMachines/jdk-18/Contents/Home/include/darwin" -dynamiclib -o implementation/libaudio-snd.dylib implementation/src/main/cpp/org/plovdev/audioengine/AudioEngine.cpp implementation/src/main/cpp/org/plovdev/audioengine/devices/AudioDeviceManager.cpp implementation/src/main/cpp/org/plovdev/audioengine/devices/NativeOutputAudioDevice.cpp -framework AudioToolbox -framework CoreAudio -framework CoreFoundation -std=c++17
