package com.trocabook.Trocabook.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// Esta classe representa a resposta JSON da API do Google reCAPTCHA
public class RecaptchaResponse {

    private boolean success;
    private double score;
    private String action;

    @JsonProperty("challenge_ts")
    private String challengeTs;

    private String hostname;

    // Getters e Setters para todos os campos

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getChallengeTs() {
        return challengeTs;
    }

    public void setChallengeTs(String challengeTs) {
        this.challengeTs = challengeTs;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }
}