# VEDA Auth Solution Evaluation Matrix

The below table refers to the previously [defined auth workflows](https://github.com/NASA-IMPACT/veda-auth-central/blob/main/docs/use-case-evaluation/workflows.md).

Our goal with this evaluation is to find out whether there is a solution that fulfils the workflows while relying on existing community solutions as much as possible,
only with IaC and durable configuration of the solution.

Building a replicable platform from community-maintained upstream solutions is a principle of the [VEDA Open Source Ecosystem](https://docs.openveda.cloud/open-source-ecosystem/).

The evaluated options are [KeyCloak](https://www.keycloak.org/) and [Apache Airavata Custos](https://airavata.apache.org/custos/), which uses KeyCloak under the hood.


## Explanation of the evaluation criteria

1. Can this workflow already be satisfied with this technology without having to *build* new things?
2. Did we (the NASA VEDA team) *build* this, in contrast to *deploying* a specific configuration of features built by an upstream community?
3. Level of effort to implement and document the workflow solution, in developer sprints (2 weeks)
4. Ongoing effort required to maintain this workflow in the future. This includes keeping up with upstream releases.
5. How easy is it to extend and modify this workflow in the future?


## Explanation of the values

To evaluate these workflows, each cell in the below shall be given

1. yes/no
2. yes/no
3. Estimated number of developer sprints (2 weeks)
4. Numerical score (1-5, where 5 is best)
5. Numerical score (1-5, where 5 is best)

## Matrix

| [Workflow](https://github.com/NASA-IMPACT/veda-auth-central/blob/main/docs/use-case-evaluation/workflows.md) | (E1) Already implemented | (E2) Built by VEDA | (E3) Level of Effort | (E4) Ongoing Maintenance Effort | (E5) Ease of future change | 
| - | - | - | - | - | - |
| W1 KeyCloak |  |  |  |  |  |  |
| W1 Custos |  |  |  |  |  |  |
| W2 KeyCloak |  |  |  |  |  |  |
| W2 Custos |  |  |  |  |  |  |
| [W3](https://docs.google.com/document/d/1Jbqj89mzKYCDRxNI5VQ1WYLWQVq_93hGW6XaSm11cGs/edit?tab=t.0#heading=h.was12i5zxbgb) KeyCloak | Yes (KeyCloak feature in preview) | No (KeyCloak builtin) | 1 sprint to document | 5 (only documentation changes required on upstream changes) | 5 (easy e.g. to expand list of permissions) |
| W3 Custos |  |  |  |  |  |  |
| W4 KeyCloak |  |  |  |  |  |  |
| W4 Custos |  |  |  |  |  |  |

## Cost estimates

The evaluation criteria include level of effort to implement. This is both for project costs but even more for project planning - how much will it take to get to a solution for a workflow. 
This assumes that there is only a smaller additional effort required for implementing a concrete use case that makes use of a given workflow.

It seems like there will be no other costs than developer time:
1. It seems like there will be no subscription costs - there seem to be different ways for us to get around paying for a new CILogon subscription https://github.com/NASA-IMPACT/veda-auth-central/issues/139
2. All solutions are/will be open-source, so no license fees either.
3. Infrastructure costs should also be negligible - probably some low-powered EC2 instances for the KeyCloak and maybe Custos services + UI, etc.
