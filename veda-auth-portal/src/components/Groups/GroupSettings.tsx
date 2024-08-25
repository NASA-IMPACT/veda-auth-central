import {
  Box,
  Flex,
  FormControl,
  Text,
  FormLabel,
  Input,
  SimpleGrid,
  Stack,
  Divider, Button,
  Table,
  Thead,
  Tbody, Tr,
  Th,
  Td, TableContainer,
  Code,
  IconButton,
  Switch
} from '@chakra-ui/react';
import { PageTitle } from '../PageTitle';
import { FiTrash2 } from "react-icons/fi";
import { ActionButton } from '../ActionButton';
import { useApi } from '../../hooks/useApi';
import { BACKEND_URL } from '../../lib/constants';
import { useEffect } from 'react';
import React from 'react';
import { useAuth } from 'react-oidc-context';
import { Group, Member } from '../../interfaces/Groups';

const MOCK_GROUP_MANAGERS = [
  {
    email: "ganning.xu@gatech.edu",
  },
  {
    email: "testemail@gmail.com"
  },
  {
    email: "bob@gmail.com"
  }
]

const MOCK_ROLES = [
  {
    name: "grafana:admin",
    description: "View dashboard."
  },
  {
    name: "stac:admin",
    description: "Can view group settings and access group resources."
  },
  {
    name: "grafana:editor",
    description: "Can view group settings and access group resources."
  }
]

interface GroupSettingsProps {
  groupId: string | undefined;
}

const LeftRightLayout = ({ left, right }: { left: React.ReactNode, right: React.ReactNode }) => {
  return (
    <SimpleGrid columns={2} spacing={8}>
      <Box>
        {left}
      </Box>
      <Box>
        {right}
      </Box>
    </SimpleGrid>
  )
}

export const GroupSettings = ({ groupId }: GroupSettingsProps) => {
  const [name, setName] = React.useState('');
  const [description, setDescription] = React.useState('');
  const [owner, setOwner] = React.useState('');
  const [groupManagers, setGroupManagers] = React.useState([]);
  const [roles, setRoles] = React.useState([]);
  const auth = useAuth();

  const customFetch = async (url: string, options?: RequestInit) => {
    const resp = await fetch(url, {
      ...options,
      headers: {
        ...options?.headers,
        'Authorization': `Bearer ${auth?.user?.access_token}`
      }
    });

    const data = await resp.json();

    return data;
  }

  useEffect(() => {
    (async () => {
      const groupBasicInfo:Group = await customFetch(`${BACKEND_URL}/api/v1/group-management/groups/${groupId}`);
      console.log(groupBasicInfo);
      setName(groupBasicInfo.name);
      setDescription(groupBasicInfo.description);
      setOwner(groupBasicInfo.owner_id);
      setRoles(groupBasicInfo.clientRoles);

      const groupMembers = await customFetch(`${BACKEND_URL}/api/v1/group-management/groups/${groupId}/members`);
      const groupManagers = groupMembers.profiles.filter((member: Member) => member.membership_type === 'ADMIN');
      setGroupManagers(groupManagers);

    })();
  }, [])



  return (
    <>
      <PageTitle size="md">Group Settings</PageTitle>
      <Text color="default.secondary">Edit group membership, roles, and other information.</Text>

      <Stack border='1px solid' borderColor='border.neutral.tertiary' rounded='xl' p={8} mt={8} divider={<Divider />} spacing={8}>

        <LeftRightLayout
          left={(
            <Text fontSize='lg'>Basic Information</Text>
          )}
          right={(
            <Stack spacing={4}>
              <FormControl color='default.default'>
                <FormLabel>Name</FormLabel>
                <Input 
                  type='text' 
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  
                />
              </FormControl>
              <FormControl>
                <FormLabel>Description</FormLabel>
                <Input 
                  type='text' 
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                />
              </FormControl>
            </Stack>
          )}
        />

        <LeftRightLayout
          left={(
            <Text fontSize='lg'>Group Owner</Text>
          )}
          right={(
            <Stack spacing={4}>
              <Text ml={2}>
                {owner}
              </Text>
            </Stack>
          )}
        />

        <LeftRightLayout
          left={(
            <>
              <Text fontSize='lg'>Group Manager (s)</Text>
              <Text color='default.secondary' mt={4} fontSize='sm'>Can edit the configuration for this group and add/remove members.</Text>
            </>
          )}
          right={(
            <>
              <Stack spacing={2}>
                {
                  groupManagers.length === 0 && (
                    <Text>No group managers</Text>
                  )
                }
                {groupManagers.map((manager: Member) => (
                  <Flex key={manager.email} align='center' justifyContent='space-between'>
                    <Text ml={2}>{manager.email}</Text>
                    <Button
                      border='1px solid'
                      borderColor="border.neutral.tertiary"
                      size='sm'
                      bg='white'
                      shadow='sm'
                    >Remove</Button>
                  </Flex>
                ))}
              </Stack>

              <Button variant='link' color='blue.400' size='sm' mt={4}>Add Manager</Button>
            </>
          )}
          />

          <Box>
            <Text fontSize='lg'>Roles</Text>
            <Text color='default.secondary' mt={4} fontSize='sm'>Choose the roles to assign to members of this group.</Text>

            <TableContainer mt={4}>
              <Table variant='simple' size='sm'>
                <Thead>
                  <Tr>
                    <Th>Role</Th>
                    <Th>Description</Th>
                    <Th />
                  </Tr>
                </Thead>
                <Tbody>
                  {roles?.map((role, index) => (
                    <Tr key={index}>
                      <Td>
                        <Code colorScheme='gray'>{role}</Code>
                      </Td>
                      <Td>{role.description}</Td>

                      <Td>
                        <IconButton
                          aria-label='Delete Role'
                          icon={<FiTrash2 />}
                          size='sm'
                          bg=""
                        />
                      </Td>
                    </Tr>
                  ))}
                </Tbody>
              </Table>
            </TableContainer>
            <Button variant='link' color='blue.400' size='sm' mt={4}>Add Manager</Button>
          </Box>

          <LeftRightLayout
            left={(
              <Text fontSize='lg'>Automatically add users to this group</Text>
            )}

            right={(
              <Flex justifyContent='flex-end'>
                  <Switch colorScheme='blackAlpha'/>
              </Flex>
            )}
          />
      </Stack>

      <Stack direction='row' mt={8} spacing={4}>
        <ActionButton
          onClick={() => {}}
        >
          Save Changes
        </ActionButton>

          <Button border='1px solid' borderColor="border.neutral.secondary">
            Archive Group
          </Button>
      </Stack>
    </>
  )
}
