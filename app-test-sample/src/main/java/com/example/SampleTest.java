package com.example;

import com.willowtreeapps.apptest.Driver;
import com.willowtreeapps.apptest.Element;
import com.willowtreeapps.apptest.FormFactor;
import com.willowtreeapps.apptest.Platform;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AppTestRunner.class)
public class SampleTest {
    private final Driver driver;

    public SampleTest(Driver driver) {
        this.driver = driver;
    }

    @Test
    @RunOn(platform = Platform.ANDROID)
    public void hasHelloWorld() {
        Element hello = driver.find("content").find("hello_world");

        assertEquals("Hello World!", hello.getText());
    }

    @Test
    public void hasTypedText() {
        Element field = driver.find("field");

        field.clearText();

        assertEquals("", field.getText());

        field.typeText("Hi There");

        assertEquals("Hi There", field.getText());
    }

    @Test
    public void hasHelloWorld2AfterClickingButton() {
        Element button = driver.find("button");
        button.click();
        Element hello2 = driver.find("hello_world2");

        assertEquals("Hello World 2!", hello2.getText());

        driver.pressBack();
        Element hello = driver.find("hello_world");

        assertEquals("Hello World!", hello.getText());
    }

    @Test
    public void has20Items() {
        Element list = driver.find("list");

        assertEquals(20, list.getCount());
        assertEquals("Item 1", list.get(0).find("text").getText());
        assertEquals("Item 20", list.get(19).find("text").getText());
    }
}
