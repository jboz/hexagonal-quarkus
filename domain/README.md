# Domain part

DDD and agnostic module, so not external framework (no quarkus deps) and minimal externals librairies.
Accept are asynchronize support with mutiny and optimize classe implementation with lombok. Maybe late some tools like apache commons.
Nothing about infrastructure like jackson or hibernate, use of mixin in infra module if needed.

Domain features are defined in gherkin language.

Living documentation of the domain is automatically generated and published.

# Testing

BDD or unit test.
