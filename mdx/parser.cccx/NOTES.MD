My current accounting is that you have used 11 days of my time. So if 12 is the upper limit, you have 1 day left. I have most of the existing tests working (failing 22 out of 131, I think) but I do not think it is a very good use of my time to get the number down to zero (or close).

Maybe it is a delicate question but I'm going to explain why. You see, when you move from the existing grammar based on legacy JavaCC to CongoCC, far more of what you are specifying is *declarative* in nature.

For example:

Suppose you have some grammar production like:

     Foobar : Foo Bar ;

We assume here that `Foobar`, `Foo`, and `Bar` are all *definite nodes*, so they unconditionally cause a node of the respective name to be created. Okay? 

So let's add that you have also specified:

     INJECT Bar : implements SomeInterface

So I'd like you to understand that in this case, to write some sort of unit tests like:

     myParser.Foobar();
     assert peekNode().getChild(1) instanceof SomeInterface;

This is utterly pointless because the assertion cannot fail! It is IMPOSSIBLE! The second child (offset 1, of course) of the `Foobar` noe is a `Bar`, which has been *specified* as implementing the given interface.

So, properly understood, the above testing code is not much more sensible than writing something like:

     Foo foo = new Foo(...);
     assert foo instanceof Foo;

The key is that the above code is *declarative* in nature. If you parsed a `Foobar`, you definitely have two child nodes, that are a `Foo` and a `Bar` and if you *declared* that the `Bar` type implements `SomeInterface`, then it does! If that `SomeInterface` requires you to have a method called `baz()`, well, it must be specified somewhere, like:

     INJECT Bar : implements SomeInterface
     {
        public void baz() {...}
     }

Because if you're not fulfilling the contract, in this case, that there is a `baz()` method then the generated code won't compile!

Note also that if you want to generate a nicer API, you can write things like:

     INJECT Foobar :
     {
        public Bar getBar() {
            return (Bar) getChild(1);
        }
     }

But again, the cast in the above `getBar` method cannot really fail, so....     

Well, it's a delicate matter maybe, but a lot of these kinds of tests, checking whether whatever node is of the expected type -- this is pointless. It really is. At least, if things are structured well, there should be no need for such things.

So, just here, the moral of the story is that what you need is not to have unit tests so much, but to use the tool maximally to *declare* what there is at each point and then many of these tests just melt away, because they become pointless.

Now, to be clear, certain tests still make sense. For example, the bug where it was not unescaping a doubled `]` character, which took like a minute to fix, it makes sense to test for these things. But to test for things that are simply impossible because of the nature of the tool and the underlying Java type system...

This leads to another point. I have to say that this kind of API, where a node has a `type()` method that returns these magic values like Type.PARENTHESES or Type.PROPERTY... this IMHO is rather ugly stuff that you should be getting rid of. For one thing, if you want to know the type of node that you are examining, the best way is to structure things so that you can use the Java type system maximally. So instead of having things like:

        if (node.type() == Type.FUNCTION)...

you really should just have something more like:

        if (node instanceof FunctionCall)...

Now, even aside from that, a key point to consider is that *by default* the CongoCC generated parser is building up a *concrete* syntax tree (CST) as it parses. It is, properly speaking, a CST rather than an AST (*Abstract Syntax Tree*) because no information is being thrown away. And this has various implications...

If it is a question of knowing whether a node contains parentheses, well... we already *automatically* have API that allows us to query that:

      if (someNode.firstChildOfType(LPAREN) != null) {we have parentheses!}

Or, a more precise question, like whether the first and last "children" are LPAREN and RPAREN:

      if (someNode.getFirstChild().getType() == LPAREN 
        && someNode.getLastChild().getType() == RPAREN) {...}

Of course, typically, you wouldn't even do that because you would have:

      if (someNode instanceof ExpressionList) {...}

Or if we have some sort of construct like: `Foo(Bar, Baz)` where `Foo`, `Bar`, and `Baz` are all expressions, we already have: `myNode.childrenOfType(Expression.class)`. If all we wanted was the arguments to the function, we could write:

      List<Expression> exps = myNode.childrenOfType(Expression.class);
      exps.subList(1,exps.size());

Now, getting back to the question of how to best use my time... I think that one thing you can do is to carefully go over the existing grammar and make sure you understand it. And if there are any questions, I will go over it.

You might like some sample code that uses the `Node.Visitor` pattern to walk the tree and do something.

Well, I mean, there is surely enough various stuff to clear up that you can use the remaining day of my time. And you can buy some more days, but I do want to have some clarity where you understand what you got for the initial investment. I figure that, with this initial investment, this should give you a big leg-up in terms of getting some very productive development patterns going.

Well, I hope that makes some of my thinking clear. I'll close this note here.