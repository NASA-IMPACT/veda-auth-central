# VEDA Auth Solution Evaluation Matrix

The below table refers to the previously [defined auth workflows](https://github.com/NASA-IMPACT/veda-auth-central/blob/main/docs/use-case-evaluation/workflows.md).

Our goal with this evaluation is to find out whether there is a solution that fulfils the workflows while relying on upstream projects as much as possible, 
i.e. pieces together existing community solutions (such as KeyCloak and maybe Airavata Custos) only with IaC and durable enough configuration.

Building a replicable platform from community-maintained upstream solutions is a principle of the [VEDA Open Source Ecosystem](https://docs.openveda.cloud/open-source-ecosystem/).


## Explanation of the evaluation criteria

1. Can this workflow already be satisfied with this technology without having to *build* new things?
2. Did we (the NASA VEDA team) *build* this, or are we simply *deploying* a specific configuration of something built by an upstream community?
3. Ongoing effort required to maintain this workflow in the future. This includes keeping up with upstream releases, 
4. How is the user experience for those who have to actually perform this workflow?
5. How easy is it to extend and modify this workflow in the future?


## Explanation of the values

To evaluate these workflows, each cell in the below shall be given

1. A numerical score (1-5) (except for col 1 and 2, which are yes/no)
2. A justification of the numerical score referencing the implementation section of the workflow document


## Matrix

| Workflow | (E1) Existing upstream features | (E2) Building ourselves? | (E3) Ongoing Maintenance Effort | (E4) User Experience | (E5) Ease of future change | 
| - | - | - | - | - | - |
| W1 KeyCloak |  |  |  |  |  |
| W1 KeyCloak + Custos |  |  |  |  |  |
| W2 KeyCloak | 
| W2 KeyCloak + Custos | 
| W3 KeyCloak | 
| W3 KeyCloak + Custos | 
| 4 KeyCloak | 
| 4 KeyCloak + Custos | 
| 5 KeyCloak |
| 5 KeyCloak + Custos |

