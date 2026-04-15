// 声明当前服务类所在包。
package com.example.openclaw.service;

// 导入 Jackson 的 JSON 节点类型。
import com.fasterxml.jackson.databind.JsonNode;
// 导入 Jackson ObjectMapper。
import com.fasterxml.jackson.databind.ObjectMapper;
// 导入 Spring 服务注解。
import org.springframework.stereotype.Service;
// 导入响应式 Flux。
import reactor.core.publisher.Flux;

// 导入动态数组实现。
import java.util.ArrayList;
// 导入迭代器接口。
import java.util.Iterator;
// 导入列表接口。
import java.util.List;

// 标记当前类是一个服务组件，用于把 SSE 事件提取成文本。
@Service
public class OpenClawSseTextAssembler {

    // 创建一个 ObjectMapper，用于解析每个 SSE 片段中的 JSON 内容。
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 从原始 SSE 文本流中提取纯文本片段。
    public Flux<String> extractTextChunks(Flux<String> rawEvents) {
        // 使用响应式链路逐步处理输入流。
        return rawEvents
                // 先去掉 `data:` 前缀。
                .map(this::stripDataPrefix)
                // 过滤掉空白内容。
                .filter(payload -> !payload.isBlank())
                // 把每个 JSON 事件解析为多个文本片段。
                .flatMap(payload -> {
                    try {
                        // 将当前 JSON 字符串解析为 JsonNode。
                        JsonNode node = objectMapper.readTree(payload);
                        // 收集当前事件中可提取的文本片段。
                        List<String> chunks = collectTextChunks(node);
                        // 把收集结果重新转成 Flux。
                        return Flux.fromIterable(chunks);
                    } catch (Exception e) {
                        // 如果当前片段无法解析为 JSON，则忽略它。
                        return Flux.empty();
                    }
                })
                // 最后过滤掉空文本片段。
                .filter(text -> text != null && !text.isBlank());
    }

    // 移除 SSE 标准中的 `data:` 前缀。
    private String stripDataPrefix(String raw) {
        // 先对 null 做兜底，再去掉首尾空白。
        String trimmed = raw == null ? "" : raw.trim();
        // 如果当前字符串以 `data:` 开头，则截去前缀。
        if (trimmed.startsWith("data:")) {
            // 返回去掉前缀后的纯载荷部分。
            return trimmed.substring(5).trim();
        }
        // 如果本身没有前缀，则直接返回。
        return trimmed;
    }

    // 从不同事件结构中提取文本内容。
    private List<String> collectTextChunks(JsonNode node) {
        // 创建结果列表。
        List<String> chunks = new ArrayList<>();
        // 尝试提取 delta 字段。
        addIfPresent(chunks, node.path("delta").asText(null));
        // 尝试提取 output_text 字段。
        addIfPresent(chunks, node.path("output_text").asText(null));
        // 尝试提取 text 字段。
        addIfPresent(chunks, node.path("text").asText(null));

        // 读取当前事件类型。
        JsonNode typeNode = node.path("type");
        // 如果 type 是字符串，则继续做事件类型级别判断。
        if (typeNode.isTextual()) {
            // 取出事件类型文本。
            String eventType = typeNode.asText();
            // 如果是输出文本增量事件，则从 delta 中读取文本。
            if ("response.output_text.delta".equals(eventType)) {
                // 追加当前增量文本。
                addIfPresent(chunks, node.path("delta").asText(null));
            }
            // 如果是完成事件，则从最终 response.output 中兜底抽取文本。
            if ("response.completed".equals(eventType)) {
                // 从 response.output 结构里继续递归抽取文本。
                addTextFromOutputArray(chunks, node.path("response").path("output"));
            }
        }

        // 从根节点 output 中提取文本。
        addTextFromOutputArray(chunks, node.path("output"));
        // 从 response.output 中提取文本。
        addTextFromOutputArray(chunks, node.path("response").path("output"));
        // 从根节点 content 中提取文本。
        addTextFromContentArray(chunks, node.path("content"));
        // 从 item.content 中提取文本。
        addTextFromContentArray(chunks, node.path("item").path("content"));
        // 从 response.content 中提取文本。
        addTextFromContentArray(chunks, node.path("response").path("content"));

        // 返回最终文本片段列表。
        return chunks;
    }

    // 从 output 数组结构中递归提取文本。
    private void addTextFromOutputArray(List<String> chunks, JsonNode outputNode) {
        // 如果不是数组，则当前结构不处理。
        if (!outputNode.isArray()) {
            // 提前返回，避免无效遍历。
            return;
        }
        // 遍历 output 数组中的每个元素。
        for (JsonNode item : outputNode) {
            // 尝试提取 item.text。
            addIfPresent(chunks, item.path("text").asText(null));
            // 递归处理 item.content。
            addTextFromContentArray(chunks, item.path("content"));
        }
    }

    // 从 content 数组结构中递归提取文本。
    private void addTextFromContentArray(List<String> chunks, JsonNode contentNode) {
        // 如果不是数组，则当前结构不处理。
        if (!contentNode.isArray()) {
            // 提前返回，避免无效遍历。
            return;
        }
        // 获取当前数组的元素迭代器。
        Iterator<JsonNode> iterator = contentNode.elements();
        // 逐个遍历数组元素。
        while (iterator.hasNext()) {
            // 取出当前元素。
            JsonNode item = iterator.next();
            // 尝试提取 text 字段。
            addIfPresent(chunks, item.path("text").asText(null));
            // 尝试提取 delta 字段。
            addIfPresent(chunks, item.path("delta").asText(null));
            // 读取 annotations 字段，兼容带注解的内容结构。
            JsonNode annotations = item.path("annotations");
            // 如果 annotations 存在且当前项包含 text，则再次兜底提取 text。
            if (annotations.isArray() && item.has("text")) {
                // 把 text 再次加入结果，兼容带注释元信息的结构。
                addIfPresent(chunks, item.path("text").asText(null));
            }
        }
    }

    // 仅当字符串存在且非空时才加入结果列表。
    private void addIfPresent(List<String> chunks, String value) {
        // 过滤 null 与空白字符串。
        if (value != null && !value.isBlank()) {
            // 将合法文本加入结果列表。
            chunks.add(value);
        }
    }
}
