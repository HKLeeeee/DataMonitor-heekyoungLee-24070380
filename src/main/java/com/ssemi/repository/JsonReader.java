package com.ssemi.repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 외부 라이브러리 없이 JSON 파일을 파싱하는 단순 리더.
 * 단순 key-value 배열 형태의 JSON만 지원한다.
 */
public class JsonReader {

    public List<Map<String, String>> readArray(Path filePath) throws IOException {
        if (!Files.exists(filePath)) {
            return new ArrayList<>();
        }
        String content = Files.readString(filePath).trim();
        return parseArray(content);
    }

    private List<Map<String, String>> parseArray(String content) {
        List<Map<String, String>> result = new ArrayList<>();
        if (content == null || content.isEmpty()) {
            return result;
        }

        content = content.trim();
        if (content.startsWith("[")) {
            content = content.substring(1);
        }
        if (content.endsWith("]")) {
            content = content.substring(0, content.lastIndexOf(']'));
        }
        content = content.trim();

        if (content.isEmpty()) {
            return result;
        }

        List<String> objects = splitObjects(content);
        for (String obj : objects) {
            Map<String, String> map = parseObject(obj.trim());
            if (!map.isEmpty()) {
                result.add(map);
            }
        }
        return result;
    }

    // 중첩을 고려하여 최상위 객체({...}) 단위로 분리
    private List<String> splitObjects(String content) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = -1;
        for (int i = 0; i < content.length(); i++) {
            char c = content.charAt(i);
            if (c == '{') {
                if (depth == 0) {
                    start = i;
                }
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start >= 0) {
                    objects.add(content.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return objects;
    }

    private Map<String, String> parseObject(String obj) {
        Map<String, String> map = new HashMap<>();
        if (!obj.startsWith("{") || !obj.endsWith("}")) {
            return map;
        }
        String inner = obj.substring(1, obj.length() - 1).trim();

        int i = 0;
        while (i < inner.length()) {
            while (i < inner.length() && Character.isWhitespace(inner.charAt(i))) i++;
            if (i >= inner.length()) break;

            if (inner.charAt(i) != '"') { i++; continue; }

            // 키 파싱 — 값과 동일하게 이스케이프 처리
            i++;
            StringBuilder keyBuilder = new StringBuilder();
            while (i < inner.length()) {
                char c = inner.charAt(i);
                if (c == '\\' && i + 1 < inner.length()) {
                    keyBuilder.append(inner.charAt(i + 1));
                    i += 2;
                } else if (c == '"') {
                    break;
                } else {
                    keyBuilder.append(c);
                    i++;
                }
            }
            if (i >= inner.length()) break;
            String key = keyBuilder.toString();
            i++; // 닫는 따옴표 이동

            while (i < inner.length() && (inner.charAt(i) == ':' || Character.isWhitespace(inner.charAt(i)))) i++;

            String value;
            if (i >= inner.length()) break;
            char valueStart = inner.charAt(i);
            if (valueStart == '"') {
                i++;
                StringBuilder sb = new StringBuilder();
                while (i < inner.length()) {
                    char c = inner.charAt(i);
                    if (c == '\\' && i + 1 < inner.length()) {
                        sb.append(inner.charAt(i + 1));
                        i += 2;
                    } else if (c == '"') {
                        break;
                    } else {
                        sb.append(c);
                        i++;
                    }
                }
                value = sb.toString();
                i++;
            } else {
                int vStart = i;
                while (i < inner.length() && inner.charAt(i) != ',' && inner.charAt(i) != '}') {
                    i++;
                }
                value = inner.substring(vStart, i).trim();
                // null 리터럴은 빈 문자열로 정규화
                if ("null".equalsIgnoreCase(value)) {
                    value = null;
                }
            }

            map.put(key, value);

            while (i < inner.length() && (inner.charAt(i) == ',' || Character.isWhitespace(inner.charAt(i)))) i++;
        }
        return map;
    }
}
