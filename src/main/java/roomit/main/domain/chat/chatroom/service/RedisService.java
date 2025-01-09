package roomit.main.domain.chat.chatroom.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@RequiredArgsConstructor
@Service
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    @PostConstruct
    public void initialize() {
        Set<String> keys = redisTemplate.keys("*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public void addToSet(String key, String value){
        redisTemplate.opsForSet().add(key,value);
    }

    public void setData(String key, Object value) {
        redisTemplate.opsForValue().set(key, value.toString());
    }

    public Object getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public Long getSetSize(String key){
        return redisTemplate.opsForSet().size(key);
    }

    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    public void deleteToSet(String key, String value){
        redisTemplate.opsForSet().remove(key, value);
    }
}
