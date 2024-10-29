package org.alex_hashtag;

import org.alex_hashtag.tokenization.TokenStream;


public class Main
{
    public static void main(String[] args)
    {
        new TokenStream("""
                \"
                \"jfirnr\"
                
                \"
                """).printTokens();
    }
}