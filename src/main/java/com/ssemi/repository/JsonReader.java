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

    /**
     * JSON 배열 파일을 읽어 각 객체를 Map<String, String>으로 반환.
     */
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

        // 바깥 배열 괄호 제거
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

        // 객체 단위로 분리
        List<String> objects = splitObjects(content);
        for (String obj : objects) {
            Map<String, String> map = parseObject(obj.trim());
            if (!map.isEmpty()) {
                result.add(map);
            }
        }
        return result;
    }

    /**
     * 중첩을 고려하여 최상위 객체({...}) 단위로 분리.
     */
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

    /**
     * { "key": "value", ... } 형태의 문자열을 Map으로 변환.
     * 값은 모두 String으로 반환.
     */
    private Map<String, String> parseObject(String obj) {
        Map<String, String> map = new HashMap<>();
        if (!obj.startsWith("{") || !obj.endsWith("}")) {
            return map;
        }
        // 양쪽 중괄호 제거
        String inner = obj.substring(1, obj.length() - 1).trim();

        // key-value 쌍 파싱
        int i = 0;
        while (i < inner.length()) {
            // 공백 스킵
            while (i < inner.length() && Character.isWhitespace(inner.charAt(i))) i++;
            if (i >= inner.length()) break;

            // 키 파싱 (따옴표로 감싸진 문자열)
            if (inner.charAt(i) != '"') { i++; continue; }
            int keyStart = i + 1;
            int keyEnd = inner.indexOf('"', keyStart);
            if (keyEnd < 0) break;
            String key = inner.substring(keyStart, keyEnd);
            i = keyEnd + 1;

            // 콜론 스킵
            while (i < inner.length() && (inner.charAt(i) == ':' || Character.isWhitespace(inner.charAt(i)))) i++;

            // 값 파싱
            String value;
            if (i >= inner.length()) break;
            char valueStart = inner.charAt(i);
            if (valueStart == '"') {
                // 문자열 값
                int vStart = i + 1;
                // 이스케이프 고려
                StringBuilder sb = new StringBuilder();
                i = vStart;
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
                i++; // 닫는 따옴표 이동
            } else {
                // 숫자/불린/null 값
                int vStart = i;
                while (i < inner.length() && inner.charAt(i) != ',' && inner.charAt(i) != '}') {
                    i++;
                }
                value = inner.substring(vStart, i).trim();
            }

            map.put(key, value);

            // 다음 쌍으로 이동 (콤마 스킵)
            while (i < inner.length() && (inner.charAt(i) == ',' || Character.isWhitespace(inner.charAt(i)))) i++;
        }
        return map;
    }
}
