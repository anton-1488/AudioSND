#ifndef AudioCodec_H
#define AudioCodec_H

#include <string>
#include <unordered_map>

namespace audiosnd {
    /**
     * Audio codec before converted to pcm
    */
    enum class AudioCodec {
        PCM8,       // 8-bit PCM
        PCM16,      // 16-bit PCM
        PCM24,      // 24-bit PCM
        PCM32,      // 32-bit PCM (integer)
        FLOAT32,    // 32-bit float
        FLOAT64,    // 64-bit float (для Hi-Res)
        OTHER
    };

     inline const char* audioCodecToString(AudioCodec codec) {
         switch(codec) {
             case AudioCodec::PCM8:      return "PCM8";
             case AudioCodec::PCM16:     return "PCM16";
             case AudioCodec::PCM24:     return "PCM24";
             case AudioCodec::PCM32:     return "PCM32";
             case AudioCodec::FLOAT32:   return "FLOAT32";
             case AudioCodec::FLOAT64:   return "FLOAT64";
             default: return "OTHER";
         }
     }

     // Для JNI конвертации (из строки)
     inline AudioCodec audioCodecFromString(const std::string& str) {
         static const std::unordered_map<std::string, AudioCodec> map = {
             {"PCM8", AudioCodec::PCM8},
             {"PCM16", AudioCodec::PCM16},
             {"PCM24", AudioCodec::PCM24},
             {"PCM32", AudioCodec::PCM32},
             {"FLOAT32", AudioCodec::FLOAT32},
             {"FLOAT64", AudioCodec::FLOAT64},
             {"OTHER", AudioCodec::OTHER}
         };

         auto it = map.find(str);
         return (it != map.end()) ? it->second : AudioCodec::OTHER;
     }
}
#endif