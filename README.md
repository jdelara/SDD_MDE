# Comparing SDD frameworks
This repository contains three projects built with three SDD frameworks: kiro, openspec, and github spec kit. Each one of them built the same application, based on these instructions: 

- Initial prompt: I am building an application to create and manage cooking recipes. The application should list recipes, organised by different tags, like vegetarian, or vegan; but the tags should be extensible. Each recipe has a name, description, ingredients, number of people and instructions. The application should allow flexible search for recipes, create new recipes, and enable annotations of the recipe. The application is mono-user, and recipes are stored locally. 

- Design instructions: The application should be Java/Swing with as few dependencies as possible. No databases, but persistence should be in JSON. The application should be created in a standard Eclipse project.

## Spec comparison metrics

### Github spec kit Totals

| Metric | Value |
|--------|------:|
| Markdown files | 8 |
| Total lines | 1,027 |
| Total words | 6,998 |
| Total chars | 49,361 |

### Openspec Totals

| Metric | Value |
|--------|------:|
| Markdown files | 10 |
| Total lines | 450 |
| Total words | 3,869 |
| Total chars | 25,293 |

### Kiro Totals

| Metric | Value |
|--------|------:|
| Markdown files | 3 |
| Total lines | 869 |
| Total words | 6,248 |
| Total chars | 45,361 |

## SDD project structure

### OpenSpec — Directory Structure

```

openspec/
    ├── config.yaml
    ├── specs/                              (empty)
    └── changes/
        └── cooking-recipe-manager/
            ├── .openspec.yaml
            ├── design.md
            ├── proposal.md
            ├── tasks.md
            └── specs/
                ├── recipe-annotation/
                │   └── spec.md
                ├── recipe-editor/
                │   └── spec.md
                ├── recipe-list-view/
                │   └── spec.md
                ├── recipe-model/
                │   └── spec.md
                ├── recipe-persistence/
                │   └── spec.md
                ├── recipe-search/
                │   └── spec.md
                └── tag-management/
                    └── spec.md
```

### Github spec kit: Specs Directory Structure

```
specs/
└── 001-recipe-manager/
    ├── spec.md
    ├── plan.md
    ├── tasks.md
    ├── research.md
    ├── data-model.md
    ├── quickstart.md
    ├── checklists/
    │   └── requirements.md
    └── contracts/
        └── json-schema.md
```


### Kiro: Specs Directory Structure

```

 .kiro/
  └── specs/
      └── recipe-manager/
          ├── .config.kiro
          ├── design.md
          ├── requirements.md
          └── tasks.md
```

## Comparison of the generated Java project

### github spec kit

| Metric              | Business | Test | Total |
|---------------------|---------:|-----:|------:|
| Files               | 14       | 0    | 14    |
| Packages            | 5        | 0    | 5     |
| Classes             | 17       | 0    | 17    |
| Interfaces          | 0        | 0    | 0     |
| Enumerations        | 0        | 0    | 0     |
| Total lines         | 1252     | 0    | 1252  |
| Blank lines         | 186      | 0    | 186   |
| Comment-only lines  | 34       | 0    | 34    |
| Code lines          | 1032     | 0    | 1032  |


### kiro

| Metric              | Business | Test | Total |
|---------------------|---------:|-----:|------:|
| Files               | 27       | 5    | 32    |
| Packages            | 6        | 2    | 6     |
| Classes             | 29       | 7    | 36    |
| Interfaces          | 4        | 1    | 5     |
| Enumerations        | 0        | 0    | 0     |
| Total lines         | 3198     | 2190 | 5388  |
| Blank lines         | 466      | 279  | 745   |
| Comment-only lines  | 626      | 263  | 889   |
| Code lines          | 2106     | 1648 | 3754  |


### openspec

| Metric              | Business | Test | Total |
|---------------------|---------:|-----:|------:|
| Files               | 12       | 0    | 12    |
| Packages            | 5        | 0    | 5     |
| Classes             | 13       | 0    | 13    |
| Interfaces          | 0        | 0    | 0     |
| Enumerations        | 0        | 0    | 0     |
| Total lines         | 1430     | 0    | 1430  |
| Blank lines         | 235      | 0    | 235   |
| Comment-only lines  | 33       | 0    | 33    |
| Code lines          | 1162     | 0    | 1162  |
