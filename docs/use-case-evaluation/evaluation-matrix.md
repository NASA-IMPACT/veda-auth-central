# VEDA Auth Solution Evaluation Matrix

The below table refers to the previously [defined auth workflows](https://github.com/NASA-IMPACT/veda-auth-central/blob/main/docs/use-case-evaluation/workflows.md).

Our goal with this evaluation is to find out whether there is a solution that fulfils the workflows while relying on upstream projects as much as possible, 
i.e. pieces together existing community solutions (such as KeyCloak and maybe Airavata Custos) only with IaC and durable enough configuration.

Building a replicable platform from community-maintained upstream solutions is a principle of the [VEDA Open Source Ecosystem](https://docs.openveda.cloud/open-source-ecosystem/).


## Explanation of the evaluation criteria

1. Can this workflow already be satisfied with this technology without having to *build* new things?
2. Did we (the NASA VEDA team) *build* this, or are we simply *deploying* a specific configuration of something built by an upstream community?
3. Ongoing effort required to maintain this workflow in the future. This includes keeping up with upstream releases
4. Level of effort to implement in developer sprints
5. How is the user experience for those who have to actually perform this workflow?
6. How easy is it to extend and modify this workflow in the future?


## Explanation of the values

To evaluate these workflows, each cell in the below shall be given

1. yes/no
2. yes/no
3. Numerical score (1-5, where 5 is best)
4. Estimated number of developer sprints
5. Numerical score (1-5, where 5 is best)
6. Numerical score (1-5, where 5 is best)

## Matrix

| [Workflow](https://github.com/NASA-IMPACT/veda-auth-central/blob/main/docs/use-case-evaluation/workflows.md) | (E1) Already implemented | (E2) Built by VEDA | (E3) Ongoing Maintenance Effort | (E4) Level of Effort | (E5) User Experience | (E6) Ease of future change | 
| - | - | - | - | - | - | - |
| W1 KeyCloak |  |  |  |  |  |  |
| W1 Custos |  |  |  |  |  |  |
| W2 KeyCloak |  |  |  |  |  |  |
| W2 Custos |  |  |  |  |  |  |
| [W3](https://docs.google.com/document/d/1Jbqj89mzKYCDRxNI5VQ1WYLWQVq_93hGW6XaSm11cGs/edit?tab=t.0#heading=h.was12i5zxbgb) KeyCloak | Yes (KeyCloak feature in preview) | No (KeyCloak builtin) | None | 0 | 4 (using KeyCloak UI) | 5 (easy e.g. to expand list of permissions) |
| [W3](https://docs.google.com/document/d/14UXEQWEouGRIUbI4epmbGSH2svYWbTKxjxNf3TR_ohw/edit?tab=t.0#heading=h.was12i5zxbgb) Custos |  |  |  |  |  |  |
| W4 KeyCloak |  |  |  |  |  |  |
| W4 Custos |  |  |  |  |  |  |

## Cost estimates

The evaluation criteria include level of effort to implement. This is both for project costs but even more for project planning - how much will it take to get to a solution for a workflow. 
This assumes that there is only a smaller additional effort required for implementing a concrete use case that makes use of a given workflow.

It seems like there will be no other costs than developer time:
1. It seems like there will be no subscription costs - there seem to be different ways for us to get around paying for a new CILogon subscription https://github.com/NASA-IMPACT/veda-auth-central/issues/139
2. All solutions are/will be open-source, so no license fees either.
3. Compute costs should also be negligible - probably some low-powered EC2 instances for the KeyCloak and maybe Custos services + UI, etc.
