package org.alex_hashtag.tokenization;

public record ImportDeclaration(boolean isStatic, String fullName, String memberOrStar)
{
    // If memberOrStar is null, it means the user did something like: import foo.bar;
    // If memberOrStar is "*", then it's a star import: import foo.bar.*;
    // Otherwise it's a single member: static import foo.bar.Baz.qux;

    @Override
    public String toString()
    {
        return (isStatic ? "static " : "") + "import " + fullName + (memberOrStar != null ? "." + memberOrStar : "");
    }
}
