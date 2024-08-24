import { useState } from 'react'
import {
  Box, Flex, Icon, Input, InputGroup, InputRightElement, Text,
  Table,
  Thead,
  Tbody, Tr,
  Th,
  Td, TableContainer
} from '@chakra-ui/react'
import { NavContainer } from '../NavContainer'
import { PageTitle } from '../PageTitle'
import { ActionButton } from '../ActionButton'
import { IoIosAdd } from "react-icons/io"
import { CiSearch } from "react-icons/ci"
import { Link } from 'react-router-dom'
import { useAuth } from 'react-oidc-context'
import { BACKEND_URL, CLIENT_ID } from '../../lib/constants'
import { useApi } from '../../hooks/useApi'
import { decodeToken } from '../../lib/util'


export const Groups = () => {
  const [search, setSearch] = useState('');
  
  const auth = useAuth();
  const userInfo = decodeToken(auth.user?.access_token);
  

  const userGroups = useApi(`${BACKEND_URL}/api/v1/group-management/users/${userInfo?.email}/group-memberships?client_id=${CLIENT_ID}`);

  console.log('userGroups', userGroups);

  return (
    <NavContainer activeTab='groups'>
      {/* TOP HEADER PORTION */}
      <Flex justifyContent='space-between' alignItems='flex-start'>
        <Box>
          <PageTitle>Groups</PageTitle>
          <Text color='gray.500' mt={2}>View and manage all groups.</Text>
        </Box>
        <ActionButton icon={IoIosAdd} onClick={() => {
          console.log('Create Group')
        }}>
          Create Group
        </ActionButton>
      </Flex>

      {/* SEARCH BOX */}
      <InputGroup mt={4}>
          <InputRightElement pointerEvents='none'>
            <Icon as={CiSearch} color='black' />
          </InputRightElement>
        <Input
          type='text'
          placeholder='Search groups'
          _focus={{
            borderColor: 'black'
          }}
          _hover={{
            borderColor: 'black'
          }}
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </InputGroup>

      {/* GROUPS TABLE */}
      <TableContainer mt={4}>
        {/* only need to show: Name, Owner, Your role, Description, number of members */}
        <Table variant='simple'>
          <Thead>
            <Tr>
              <Th>Name</Th>
              <Th>Owner</Th>
              <Th>Your Role</Th>
              <Th>Description</Th>
              <Th>Members</Th>
            </Tr>
          </Thead>
          <Tbody>
            {
              // eslint-disable-next-line @typescript-eslint/no-explicit-any
              userGroups.data?.groups?.map((group: any) => {
                return (
                  <Tr key={group.id}>
                    <Td>
                      <Link
                        to={`/groups/${group.id}`}
                      >
                        <Text
                          color='blue.400'
                          _hover={{
                            color: 'blue.600',
                            cursor: 'pointer'
                          }}
                        >
                          {group.name}
                        </Text>
                      </Link>
                    </Td>
                    <Td>{group.owner_id}</Td>
                    <Td>{group.your_role}</Td>
                    <Td>{group.description}</Td>
                    <Td>{group.num_members}</Td>
                  </Tr>
                )
              })
            }
          </Tbody>
        </Table>
      </TableContainer>


    </NavContainer>
  )
}
