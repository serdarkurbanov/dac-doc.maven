# What is this file?
This file tests usage of **dacdoc-maven-plugin**. 
Put as many markdown elements as possible here

## Using DACDOC in header. [self](./README.md) ![55c40a72-aa57-4f9f-b137-525a2db8989c](dacdoc-resources/circle-red-12px.png "sample comment")

## Using DACDOC in regular text
This is link we need to test (default url check): [self](./README.md) ![55c40a72-aa57-4f9f-b137-525a2db8989c](dacdoc-resources/circle-red-12px.png "sample comment")

This is link we need to test (url check that specifies id): [self](./README.md) ![self-check-1](dacdoc-resources/circle-red-12px.png "sample comment")

This is link we need to test (url check that specifies id and other properties): [self](./README.md) ![self-check-2](dacdoc-resources/circle-red-12px.png "sample comment")

This is piece of text we didn't write test for: some text ![non-existing-check](dacdoc-resources/circle-grey-12px.png "sample comment")

This is composite check: ![ec16487f-6757-4e22-b1cb-0ef6db44084d](dacdoc-resources/circle-red-12px.png "sample comment")

This is composite check that should appear orange: ![0f370e6b-e2c5-4317-90eb-14baa73a9051](dacdoc-resources/circle-orange-12px.png "sample comment")

Proof that without framing `!` signs, DACDOC is not a keyword: DACDOC{[self](./README.md)}

## Using DACDOC in tables
Table:

| column A      | column B |
| ------------- | -----:|
| checking connection to [google](https://www.google.com) ![f5b1b84a-0250-4fe4-98f8-084acde933d5](dacdoc-resources/circle-red-12px.png "sample comment")      | now connection to [stackoverflow](https://stackoverflow.com) ![83b15bbf-440f-4914-aeb7-5500bcd2cda8](dacdoc-resources/circle-red-12px.png "sample comment") |

## Using DACDOC in lists
List:
* connection to self [self](./README.md) ![55c40a72-aa57-4f9f-b137-525a2db8989c](dacdoc-resources/circle-red-12px.png "sample comment")

  indented link: [self](./README.md) ![55c40a72-aa57-4f9f-b137-525a2db8989c](dacdoc-resources/circle-red-12px.png "sample comment")
    
# Inline code
`some inline code here` ![2568033c-b66e-4f55-9e23-ceb05b206abe](dacdoc-resources/circle-grey-12px.png "sample comment")


# Block of code

```java
some block of code here
```
 ![9c22089b-e9e4-4584-8d0c-22b2cdb7f161](dacdoc-resources/circle-grey-12px.png "sample comment")
