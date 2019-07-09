# Whta is this file?
This file tests usage of **dacdoc-maven-plugin**. 
Put as many markdown elements as possible here

## Using DACDOC in header. !DACDOC{[self](./README.md)}!

## Using DACDOC in regular text
This is link we need to test (default url check): !DACDOC{[self](./README.md)}!

This is link we need to test (url check that specifies id): !DACDOC{[self](./README.md)}(id=self-check-1;test=dacdoc-url)!

This is link we need to test (url check that specifies id and other properties): !DACDOC{[self](./README.md)}(id=self-check-2;test=dacdoc-url)!

This is piece of text we didn't write test for: !DACDOC{some text}(id=non-existing-check; test=non-existing-test)!

This is composite check: !DACDOC(ids=self-check-1, self-check-2)!

This is composite check that should appear orange: !DACDOC(ids=self-check-1, self-check-2, non-existing-test)!

Proof that without framing `!` signs, DACDOC is not a keyword: DACDOC{[self](./README.md)}

## Using DACDOC in tables
Table:

| column A      | column B |
| ------------- | -----:|
| checking connection to !DACDOC{[google](https://www.google.com)}!      | now connection to !DACDOC{[stackoverflow](https://stackoverflow.com)}! |

## Using DACDOC in lists
List:
* connection to self !DACDOC{[self](./README.md)}!

  indented link: !DACDOC{[self](./README.md)}!
    
# Inline code
!DACDOC{`some inline code here`}(test=non-existing-test)!


# Block of code
!DACDOC{
```java
some block of code here
```
}(test=non-existing-test)!
