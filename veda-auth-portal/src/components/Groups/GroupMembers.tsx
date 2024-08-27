import {
  Text,
  Box,
  Flex,
  useToast,
  Button,
  Icon,
  Input,
  InputGroup,
  InputRightElement,
  Table,
  TableContainer,
  Tbody,
  Td, Th,
  Thead,
  Tr, useDisclosure
} from '@chakra-ui/react';
import { PageTitle } from '../PageTitle';
import { ActionButton } from '../ActionButton';
import { CiSearch } from 'react-icons/ci';
import { IoIosAdd } from "react-icons/io";
import { useApi } from '../../hooks/useApi';
import { BACKEND_URL } from '../../lib/constants';
import axios from 'axios';
import React from 'react';
import { useAuth } from 'react-oidc-context';
import { AddGroupMemberModal } from './AddGroupMemberModal';
import { Member } from '../../interfaces/Groups';
import { useNavigate } from 'react-router-dom';

interface GroupMembersProps {
  groupId: string | undefined;
}

export const GroupMembers = ({ groupId }: GroupMembersProps) => {
  const toast = useToast();
  const { isOpen, onOpen, onClose } = useDisclosure()
  const [search, setSearch] = React.useState('');
  const auth = useAuth();
  const navigate = useNavigate();

  const groupMembers = useApi(`${BACKEND_URL}/api/v1/group-management/groups/${groupId}/members`);

  
    console.log(groupMembers.data?.profiles);
    let filteredMembers = [];

    if (!groupMembers.isPending && groupMembers.data) {
      const lowerSearch = search.toLowerCase();
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      filteredMembers = groupMembers.data.profiles.filter((member: any) => {
        return member.first_name.toLowerCase().includes(lowerSearch)
          || member.last_name.toLowerCase().includes(lowerSearch)
          || member.email.toLowerCase().includes(lowerSearch);
      })
    }

  const handleRemoveMember = async (email: string) => {
    const resp = await axios.delete(`${BACKEND_URL}/api/v1/group-management/groups/${groupId}/members/${email}`, {
      headers: {
        Authorization: `Bearer ${auth.user?.access_token}`
      }
    });

    if (resp.status > 199 && resp.status < 300) {
      toast({
        title: 'Member removed',
        status: 'success',
        duration: 2000,
        isClosable: true
      })
    } else {
      toast({
        title: 'Error removing member',
        status: 'error',
        duration: 2000,
        isClosable: true
      })
    }
  }

  if (groupMembers.isPending) {
    return <Text>Loading...</Text>
  }

  return (
    <>
      <Box bg='gray.100' p={4} rounded='lg'>
        <PageTitle size="md">Invitation Link</PageTitle>
        <Text color="default.secondary" mt={2}>Anyone with the link can sign up.</Text>

        <Flex alignItems='center' my={6} gap={2}>
          <Text bg='white' py={1} px={2} rounded='md'>https://veda-auth.org/invite/g113s-qw5fs-ffa12-4454a</Text>

          <ActionButton
            onClick={() => {
              navigator.clipboard.writeText('https://veda-auth.org/invite/g113s-qw5fs-ffa12-4454a')

              toast({
                title: 'Link copied',
                status: 'success',
                duration: 2000,
                isClosable: true
              })
            }}
            size='sm'
          >
            Copy Link
          </ActionButton>
        </Flex>

        <Button variant='link' color='blue.400' size='sm'>Disable invitation link</Button>
      </Box>

      <Box mt={8}>
        <PageTitle size="md">Members</PageTitle>
        <Text color="default.secondary" mt={2}>Members in this group.</Text>


      {/* SEARCH BOX AND ADD */}
      <Flex alignItems='center' justifyContent='space-between' mt={4} gap={4}>
        <InputGroup maxW='400px'>
            <InputRightElement pointerEvents='none'>
              <Icon as={CiSearch} color='black' />
            </InputRightElement>
          <Input
            type='text'
            placeholder='Search members'
            rounded='md'
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
        <Box>
          <ActionButton
            icon={IoIosAdd}
            onClick={onOpen}>
              Add members
            </ActionButton>
        </Box>

        <AddGroupMemberModal isOpen={isOpen} onClose={onClose} groupId={groupId}  />


      </Flex>

      <TableContainer mt={8}>
        <Table size='md'>
          <Thead>
            <Tr>
              <Th>Name</Th>
              <Th>Role</Th>
              <Th>Email</Th>
              <Th>Action</Th>
            </Tr>
          </Thead>
          <Tbody>
            {
              // eslint-disable-next-line @typescript-eslint/no-explicit-any
              filteredMembers.map((member: Member) => (
                <Tr key={member.email}>
                  <Td>{`${member.first_name} ${member.last_name}`}</Td>
                  <Td>{member.membership_type}</Td>
                  <Td>{member.email}</Td>
                  <Td>
                    <Button
                      variant='link'
                      color='blue.400'
                      size='sm'
                      onClick={() => {
                        handleRemoveMember(member.email);
                        navigate(0);
                      }}
                    >
                      Disable
                    </Button>
                  </Td>
                </Tr>
              ))
            }
          </Tbody>
        </Table>
      </TableContainer>

      </Box>
    </>
  )
}
