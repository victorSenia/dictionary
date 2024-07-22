package org.leo.dictionary;

import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {WindowsModule.class})
interface WindowsAppComponent {
    WindowsApp buildWindowsApp();
}
