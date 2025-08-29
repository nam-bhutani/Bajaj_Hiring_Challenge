package com.example.demo;

import java.util.Objects;

public class SolutionRequest {
    private String finalQuery;

    public SolutionRequest() {
    }

    public SolutionRequest(String finalQuery) {
        this.finalQuery = finalQuery;
    }

    public String getFinalQuery() {
        return finalQuery;
    }

    public void setFinalQuery(String finalQuery) {
        this.finalQuery = finalQuery;
    }

    @Override
    public String toString() {
        return "SolutionRequest{" +
                "finalQuery='" + finalQuery + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SolutionRequest that = (SolutionRequest) o;
        return Objects.equals(finalQuery, that.finalQuery);
    }

    @Override
    public int hashCode() {
        return Objects.hash(finalQuery);
    }
}