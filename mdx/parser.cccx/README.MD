The grammar/parser here is a work in progress.

## Issues:

There does not seem to be a standardized MDX syntax. I have noticed that the version of MDX supported by Microsoft frequently differs quite flagrantly from the one that comes from Oracle. Also, sometimes both of those differ both from each other and the [grammar that I am using as a starting point.](https://github.com/pentaho/mondrian/blob/master/mondrian/src/main/java/mondrian/parser/MdxParser.jj), which seems to be from Pentaho.

Here are a few examples:

The spec from Microsoft (see [here](https://learn.microsoft.com/en-us/sql/mdx/arithmetic-operators?view=sql-server-ver16)) specifies 5 arithmetic operators, the basic four of multiplication, division, addition and subtraction, and also exponentiation (`^`). The spec from Oracle has the same basic four but does not have exponentiation. See [here](https://docs.oracle.com/en/database/other-databases/essbase/21/esscq/mdx-operators.html#GUID-9DDD21C5-FD4B-47A6-BAAF-B99F7D009943). The parser from Pentaho also does not have exponentiation. However, it has a *concatenation operator*, `||` that is not specified in either the Microsoft or the Oracle implementation. See [here](https://github.com/pentaho/mondrian/blob/master/mondrian/src/main/java/mondrian/parser/MdxParser.jj#L670). Neither the Microsoft or Oracle spec mention this. Oh, and Oracle also has a `%` operator that, surprisingly, is not modulus, but actually **is** percentage. It seems that `A%B` is basically syntactic sugar for `100*A/B`. But that is not in the Microsoft spec, nor is it implemented in the Pentaho grammar.

In terms of multiline comments, the Microsoft spec says clearly that they can nest, so you can have `/* Outer /* Inner */ More Outer */`. That does not work in either the Oracle or Pentaho spec, i.e. the comment would end after the first `*/`. I did manage to resolve the whole issue (quite thoroughly IMO) by having both nested and non-nested comments working, though you have to configure the one you want. See [here](https://github.com/revusky/workground/blob/work/mdx/parser.cccx/src/main/ccc/Lexer.inc.ccc#L20) and also line 48 in the same file.

So, all of this just boils down to which spec are we implementing? It may not be so hard to make the various things configurable. Well, in the case of the nested comments, that was easy enough to do. Perhaps the thing to do with the different operators is just to support all of them, but then, of course, the `^` operator that exists in the Microsoft implementation would be parsed, but if one is using the Oracle implementation, that construct won't work.

## Some other things

Various of the interfaces in the packages `org.eclipse.mdx.model.api.*` have methods that return `java.util.List` of some type or other. Just for example, consider [the code here](https://github.com/revusky/workground/blob/work/mdx/model.api/src/main/java/org/eclipse/daanse/mdx/model/api/select/CreateMemberBodyClause.java#L27). It seems to me that this should really be:

     List<? extends MemberPropertyDefinition> memberPropertyDefinitions();

And pretty much all similar API's should use the extends wildcard. You see, if the method specifies that it returns `List<MemberPropertyDefinition`> which is really `List<org.eclipse.daanse.mdx.model.api.select.MemberPropertyDefinition>` this means that we cannot satisfy the contract by returning, for example: `List<org.eclipse.daanse.mdx.parser.ccc.tree.MemberPropertyDefinition>`. This is because `List<org.eclipse.daanse.mdx.parser.ccc.tree.MemberPropertyDefinition>` is NOT assignable to `List<org.eclipse.daanse.mdx.model.api.select.MemberPropertyDefinition>` but if we specify `List<? extends MemberPropertyDefinition>` in the interface then it is assignable. *You see?* 

Well, that is a typical subtlety in terms of using generics. But another general point I would make about this is that there really isn't much of a problem with naming collisions. You see, if we have an interface that is API_PACKAGE.SomeName and a concrete implementation that is NODE_PACKAGE.SomeName,there really isn't a problem with reusing the same name for the interface and the concrete node. And given that we have covariant return types since JDK 5 (I just looked that up!) if you have a method in your API that returns the more abstract type (i.e. the interface), that can be *implemented* by a method that returns the concrete subtype. So if your API says:

     API_PACKAGE.SomeName apiMethodName();

This can be *implemented* in a concrete node via:

     NODE_PACKAGE.SomeName apiMethodName() {
         return firstChildOfType(SomeName.class);
     }

I mean, since the more specialized subtype NODE_PACKAGE.SomeName is assignable to the interface API_PACKAGE.SomeName there is no problem.

There is also no problem with having method names that are the same as type names in Java. I mean, you can have:

     public Foobar Foobar() {...}

perfectly well because the first `Foobar` is the type and the second `Foobar` is the method, and they exist in completely different namespaces and certainly do not step on one another. 

Now, it's true that reusing the same names is arguably confusing. Maybe somewhat, but it is easy to exaggerate. You see, really, in any modern IDE, you just hover over a variable name with the mouse and it gives you the fully qualified type. So it's basically a non-problem. To solve this *ersatz* problem by having all this ugliness of having to name your parser productions `parseFoo` and your generated node type `ASTFoo` (or something like that) is just not a good trade-off IMHO. And again, I don't think that reusing the same names for the API as the concrete node (that implements the API) really does lead to confusing code. For the most part, the whole thing follows the principle of least surprise quite well. And this way, we end up with a quite clean looking readable grammar -- compared to the starting point anyway!

