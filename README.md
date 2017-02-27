MLNF
====

*A Framework for Link Discovery based on Markov Logic Networks.*

## Requirements

* Java 1.8+
* PostgreSQL 9.4.x
* Gurobi solver
* Maven
* Wget, Unzip

## Experiments

The following command will discover new links of any predicate (`--aim`) on the WordNet dataset (`--input`) with mining threshold 0.8 (`--mining`) and 1 million Gibbs sampling iterations (`--sampling`).

```bash
java -Xmx1g -jar target/MLNF-0.5.0-jar-with-dependencies.jar plain --input data/benchmark/wn18/wordnet-mlj12-train.nt,data/benchmark/wn18/wordnet-mlj12-valid.nt --output wn18/ --mining 0.8 --sampling 1000000 --aim "*"
```

Discovered links can be found in the `--output` folder at `./wn18/discovered_X.nt`, where `X` is the output threshold, meaning that a file contains all links whose confidence is greater or equal than `X`.

An excerpt of the discovered **rules and weights**:

```text
0.990517419  wn18:_part_of(b, a) => wn18:_has_part(a, b)
0.862068966  wn18:_instance_hypernym(a, c) AND wn18:_synset_domain_topic_of(f, b) => wn18:_synset_domain_topic_of(a, b)
```

An excerpt of the discovered **links** with confidence > 0.9:

```text
wn18:08131530 wn18:_has_part wn18:08132046 .
wn18:09189411 wn18:_has_part wn18:08707917 .
wn18:10484858 wn18:_synset_domain_topic_of wn18:08441203 .
wn18:01941987 wn18:_synset_domain_topic_of wn18:00300441 .
```

### Basic documentation

MLNF can be launched as follows.

```bash
java -Xmx1g -jar target/MLNF-0.5.0-jar-with-dependencies.jar <GOAL> <PARAMETERS>
```

#### Goals

**Goal**|**Description**
:-----|:-----
`plain`|Launch a plain execution.
`eval`|Evaluate MRR and hits@k.

#### Plain execution

Parameters for `plain` goal:

**Parameter**|**Description**|**Example value**
:-----|:-----|:-----
`--input`|Comma-separated N-Triple files.|`data1.nt,data2.nt`
`--output`|Workspace and output folder.|`experiment1/`
`--aim`|Aim predicate. For all predicates use wildcard `*`.|`http://www.w3.org/2002/07/owl#sameAs`
`--mining`|Rule mining threshold.|`0.9` (default: `0.0` support)
`--sampling`|Gibbs sampling iterations.|`1000000` (default: 100 x evidence size)
`--rules`|Maximum number of rules.|`1500` (default: none)
`--sim`|Enable similarity among literals as `min,step,max`.|`0.8,0.1,0.9` (default: none)
`--onto`|Enable ontology import.|`true` (default: `false`)
`--fwc`|Enable forward-chain.|`true` (default: `false`)

#### Evaluation

The `eval` goal takes two parameters: the N-Triples file of the test set and MLNF's output directory.

Example run:

```bash
java -Xmx1g -jar target/MLNF-0.5.0-jar-with-dependencies.jar eval data/benchmark/wn18/wordnet-mlj12-test.nt wn18/
```

## Install

* Get PostgreSQL 9.4.x - [Ubuntu/Debian binaries](http://oscg-downloads.s3.amazonaws.com/packages/postgresql-9.4.8-1-x64-bigsql.deb)
* Insert PostgreSQL setting parameters into a file `./mlnf.properties`. Example:

```properties
# GENERAL CONFIGURATION
pgsql_home=/usr/local/Cellar/postgresql/9.4.1
pgsql_username=postgres
pgsql_password=
pgsql_url=localhost
```

* Initialize and start database

```bash
sh pgsql-init.sh
sh pgsql-start.sh
sh pgsql-create.sh
```

## License(s)

**MLNF** is licensed under GNU General Public License v2.0.
**AMIE** is licensed under Creative Commons Attribution-NonComercial license v3.0.
**ProbKB** is licensed under the BSD license.
**RockIt** is licensed under the MIT License.
**Gurobi** can be activated using a [free academic license](http://www.gurobi.com/academia/academia-center).
