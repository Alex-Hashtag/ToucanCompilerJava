package org.alex_hashtag;

import org.alex_hashtag.tokenization.TokenStream;


public class Main
{
    public static void main(String[] args)
    {
        new TokenStream("""
                template <type T, type U>
                typedef PairList = ArrayList<tuple<T, U>>
                {
                	public static PairList new() return ArrayList<T, U>.new();
                }
                
                void main()
                {
                    PairList<String, int> pairs = PairList<String, int>.new();
                }
                """).printTokens();
    }
}