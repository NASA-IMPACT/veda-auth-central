# VEDA Auth Use Cases

<table>
  <tr>
    <th rowspan="2">#</th>
    <th rowspan="2">Use Case</th>
    <th rowspan="2">User Group (Actors)</th>
    <th rowspan="2">Validating Application (Relying Party)</th>
    <th rowspan="2">Auth Requirements</th>
    <th rowspan="2">Credential Needs</th>
    <th rowspan="2">Current VEDA Use Case (yes/no - name audience and current solution)</th>
    <th rowspan="2">Contact Person</th>
    <th colspan="5">Evaluation Criteria</th> <!-- Evaluation Criteria with sub-columns -->
  </tr>
  <tr>
    <th>Time to Implement</th>
    <th>Technical Experience</th>
    <th>Maintenance Effort</th>
    <th>Scalability</th>
    <th>Flexibility</th>
  </tr>

  <!-- Use Case 1 -->
  <tr>
    <td>1</td>
    <td><b>User profile and permission management</b> <br><br> A platform administrator can decide which users and groups have what permissions across services</td>
    <td>Platform admins <br><br> Members of a non-technical ops team</td>
    <td>IAM / Auth admin interface</td>
    <td>An admin is able to manage permissions for users and groups</td>
    <td></td>
    <td>No</td>
    <td>Overall platform needs / Jonas / Brian</td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>

  <!-- Use Case 2 -->
  <tr>
    <td>2</td>
    <td><b>STAC data CRUD - yes/no</b> <br><br> A data provider for VEDA has a number of datasets or new items for existing datasets and wants to publish them in the VEDA STAC. They login and get authorized to publish to selected catalogs</td>
    <td>Data editors <br><br> e.g. scientists affiliated with the project or data ingestion support folks</td>
    <td>STAC API, STAC Admin</td>
    <td>E.g. scopes yes/no</td>
    <td></td>
    <td>Yes <br><br> Currently relying on AWS user accounts / Cognito to authorize?</td>
    <td>Data Services team / Saadiq and Alexandra / Anthony L</td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>

  <!-- Use Case 3 -->
  <tr>
    <td>3</td>
    <td><b>STAC data CRUD - limited content</b> <br><br> A data provider can perform CRUD operations on a STAC catalog, but limited to certain collections (which they created or a group they belong to)</td>
    <td>Data editors <br><br> e.g. scientists affiliated with the project or data ingestion support folks</td>
    <td>STAC</td>
    <td>Where would the info be stored, which collections a user may access?</td>
    <td></td>
    <td></td>
    <td>Data Services team / Saadiq and Alexandra, Anthony L</td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>

  <!-- Use Case 4 -->
  <tr>
    <td>4</td>
    <td><b>Access to VEDA Hub</b></td>
    <td>Users of VEDA Hub<br><br> Scientists affiliated with the respective project (EIS, MAAP, GHG), participants of workshops who get access for the duration of the workshop.</td>
    <td>JupyterHub</td>
    <td>OAuth2 + Some group ownership information to determine who has access to what</td>
    <td></td>
    <td>Yes. Currently using GitHub team membership.</td>
    <td>Data Services team / Saadiq and Alexandra, Anthony L</td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>

  <!-- Use Case 5 -->
  <tr>
    <td>5</td>
    <td><b>Group membership management</b> <br><br> A platform administrator can add and remove users from a group they manage <br><br> Focus use case: group membership administration for JupyterHub access <br><br> As a workshop lead, I am able to log into a simple enough interface and administer membership for a group of participants, so I can get workshop participants set up with the resources they need.</td>
    <td>Privileged platform users, e.g. the host of a workshop that is relying on VEDA JupyterHub</td>
    <td>IAM / Auth admin interface</td>
    <td>
        <ul>
            <li>A privileged user is able to manage group membership for a predefined group</li>
            <li>The privileged user does NOT need to be able to create the group and edit its permissions - that an ops team member can do.</li>
        </ul>
    </td>
    <td></td>
    <td>Yes, we currently use GitHub teams on arbitrary GitHub orgs and through these delegate membership management to workshop hosts</td>
    <td>JupyterHub team / Sanjay</td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>

  <!-- Use Case 6 -->
  <tr>
    <td>6</td>
    <td><b>Elevated compute privileges in VEDA Hub</b> <br><br> Some groups of users of VEDA Hub can start bigger machines and run longer sessions than others.</td>
    <td>Scientist users of VEDA Hub that belong to a selected group</td>
    <td>JupyterHub</td>
    <td>OAuth2 + Group information</td>
    <td></td>
    <td>No</td>
    <td>JupyterHub team / Sanjay</td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>

  <!-- Use Case 7 -->
  <tr>
    <td>7</td>
    <td><b>User resource tracking within VEDA Hub</b> <br><br> We need to be able to distinguish resource consumption (core/RAM hours) for different groups of users of VEDA Hub.</td>
    <td>Workshop participants, scientist users in different programs that VEDA provides with compute resources (e.g. MAAP, SARP)</td>
    <td></td>
    <td>OAuth2 + Group information</td>
    <td></td>
    <td>No</td>
    <td>JupyterHub team / Sanjay</td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>

  <!-- Use Case 8 -->
  <tr>
    <td>8</td>
    <td><b>Access to hybrid compute environment</b> <br><br> Users who are allowed to access HPC resources from within VEDA Hub should get their user credentials forwarded into the HPC environment</td>
    <td>Select data producers</td>
    <td>HPC</td>
    <td>Device Auth Flow</td>
    <td></td>
    <td></td>
    <td>Science Support team / Alex M</td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>

  <!-- Use Case 9 -->
  <tr>
    <td>9</td>
    <td><b>Storing credentials to use within platform services</b> <br><br> Users of compute services such as VEDA Hub and DPS need to pass secrets into these services</td>
    <td>Scientist users of compute services such as VEDA Hub and DPS</td>
    <td>N/A</td>
    <td>Device Auth Flow</td>
    <td></td>
    <td>No</td>
    <td>Science Support team / Alex M</td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>

  <!-- Use Case 10 -->
  <tr>
    <td>10</td>
    <td><b>Access to DPS from VEDA Hub</b> <br><br> A group of users that is allowed to use DPS can log into JupyterHub and easily enough connect to DPS (JPL-maintained data processing service - view jobs, submit jobs, cancel jobs)</td>
    <td>Scientist users (e.g. MAAP) who are allowed to use DPSS</td>
    <td>MAAP DPS</td>
    <td>Device Auth Flow to get additional scopes</td>
    <td></td>
    <td>Soon <br><br> EIS group needs to move to VEDA instead of MAAP ADE <br><br> MAAP ADE is the only place right now where users can run DPS jobs.</td>
    <td>Science Support team / Alex M</td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>

  <!-- Use Case 11 -->
  <tr>
    <td>11</td>
    <td><b>Access to protected STAC catalog</b> <br><br> Scientists affiliated with MAAP have a catalog that is private to that group, so they can share data among each other. Only members of the program should be able to access the catalog (read, write, edit)</td>
    <td>Scientist users</td>
    <td>STAC</td>
    <td>e.g. scopes</td>
    <td></td>
    <td>No <br><br> MAAP has a private shared STAC, no? When MAAP users move to VEDA Hub, they will have this need.</td>
    <td>Science Support team / Alex M / Anthony L</td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>

  <!-- Use Case 12 -->
  <tr>
    <td>12</td>
    <td><b>Access to protected Dashboard (e.g. GHG Center staging) that allows select users to see protected data or pre-release information</b> <br><br> Only approved reviewers of pre-release information e.g. on the GHG Center website (VEDA UI) should be able to access the site and or retrieve the data from the protected API endpoints (currently not access-restricted)</td>
    <td>Data producers</td>
    <td>Web browser</td>
    <td></td>
    <td></td>
    <td>Yes - we just have https://ghg-demo.netlify.app/ behind a password</td>
    <td>UI team / Anthony B </td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>

  <!-- Use Case 13 -->
  <tr>
    <td>13</td>
    <td><b>Dashboard content publication (data stories, dataset overviews, etc.)</b> <br><br> Only authorized content creators for the VEDA Dashboard should be able to submit new content for review and publication.</td>
    <td>Content creators</td>
    <td>GitHub</td>
    <td></td>
    <td></td>
    <td>Yes <br><br> GHG Center content contributors at IMPACT <br><br> EIS content contributors publishing on VEDA Dashboard <br><br> Currently relying on GitHub flow for config repos like veda-config <br><br> Considering to add CMS-like functionality</td>
    <td>UI team / Anthony B</td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
    <td></td>
  </tr>
</table>

### Evaluation Criteria Summary

- **Time to Implement**: How quickly the solution can be set up and deployed
- **Technical Experience**: Level of expertise required for initial setup and ongoing use
- **Maintenance Effort**: How much ongoing upkeep is needed to keep the solution functional
- **Scalability**: How well the solution can handle growth in users, data, and operations
- **Flexibility**: How easily the solution can adapt to changes or be customized for different needs


