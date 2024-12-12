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

| [Workflow](https://github.com/NASA-IMPACT/veda-auth-central/blob/main/docs/use-case-evaluation/workflows.md) | (E1) Already implemented | (E2) Built by VEDA | (E3) Ongoing Maintenance Effort | (E4) User Experience | (E5) Ease of future change | 
| - | - | - | - | - | - |
| [W1](https://github.com/NASA-IMPACT/veda-auth-central/blob/main/docs/use-case-evaluation/workflows.md#workflow-1-configuring-the-upstream-authentication-provider) KeyCloak |  |  |  |  |  |
| [W1](https://github.com/NASA-IMPACT/veda-auth-central/blob/main/docs/use-case-evaluation/workflows.md#workflow-1-configuring-the-upstream-authentication-provider) Custos |  |  |  |  |  |
| [W2](https://github.com/NASA-IMPACT/veda-auth-central/blob/main/docs/use-case-evaluation/workflows.md#workflow-2-provisioning-and-configuring-authentication-clients) KeyCloak | 
| [W2](https://github.com/NASA-IMPACT/veda-auth-central/blob/main/docs/use-case-evaluation/workflows.md#workflow-2-provisioning-and-configuring-authentication-clients) Custos | 
| [W3](https://github.com/NASA-IMPACT/veda-auth-central/blob/main/docs/use-case-evaluation/workflows.md#workflow-3-assigning-users-to-groups-via-a-ui) KeyCloak | 
| [W3](https://github.com/NASA-IMPACT/veda-auth-central/blob/main/docs/use-case-evaluation/workflows.md#workflow-3-assigning-users-to-groups-via-a-ui) Custos | 
| [W4](https://github.com/NASA-IMPACT/veda-auth-central/blob/main/docs/use-case-evaluation/workflows.md#workflow-4-expose-roles--capabilities-to-services) KeyCloak | 
| [W4](https://github.com/NASA-IMPACT/veda-auth-central/blob/main/docs/use-case-evaluation/workflows.md#workflow-4-expose-roles--capabilities-to-services) Custos | 