package com.jeremy.util;

public class RandomCharacter {
    public static char getRandomCharacter(char ch1,char ch2){
        return (char)(ch1+Math.random()*(ch2-ch1+1));//因为random<1.0，所以需要+1，才能取到ch2
    }
    public static char getRandomLowerCaseLetter(){
        return getRandomCharacter('a','z');
    }
    public static char getRandomUpperCaseLetter(){
        return getRandomCharacter('A','Z');
    }
    public static char getRandomDigitLetter(){
        return getRandomCharacter('0','9');
    }

    public static char getRandomCharacter(){
        return getRandomCharacter('\u0000','\uFFFF');
    }
}
