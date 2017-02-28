package com.example.alex.motoproject;

import com.example.alex.motoproject.screenChat.ChatFragment;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = PresenterModule.class)
@Singleton
public interface PresenterComponent {
    void inject(ChatFragment view);
}