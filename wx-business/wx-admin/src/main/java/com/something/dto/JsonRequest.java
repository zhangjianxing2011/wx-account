package com.something.dto;

import lombok.Data;

import java.util.List;

@Data
public class JsonRequest {
    private List<Content> contents;

    public JsonRequest(List<Content> contents) {
        this.contents = contents;
    }

    @Data
    public static class Content {
        private List<Part> parts;

        public Content(List<Part> parts) {
            this.parts = parts;
        }
    }

    @Data
    public static class Part {
        private String text;

        public Part(String text) {
            this.text = text;
        }
    }
}
