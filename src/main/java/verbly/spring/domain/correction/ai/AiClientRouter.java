package verbly.spring.domain.correction.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AiClientRouter {

    private final Map<AiProvider, AiProviderClient> clients;

    @Value("${ai.provider:gemini}")
    private String configuredProvider;

    public AiProvider currentProvider() {
        return AiProvider.from(configuredProvider);
    }

    public AiProviderClient current() {
        AiProvider p = currentProvider();
        AiProviderClient c = clients.get(p);
        if (c == null) {
            c = clients.get(AiProvider.OPENAI);
        }
        return c;
    }

    @Bean
    public static Map<AiProvider, AiProviderClient> aiClientsMap(List<AiProviderClient> list) {
        Map<AiProvider, AiProviderClient> map = new EnumMap<>(AiProvider.class);
        for (AiProviderClient c : list) {
            map.put(c.provider(), c);
        }
        return map;
    }
}
