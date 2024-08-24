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
  Td,
  Tfoot,
  Th,
  Thead,
  Tr
} from '@chakra-ui/react';
import { PageTitle } from '../PageTitle';
import { ActionButton } from '../ActionButton';
import { CiSearch } from 'react-icons/ci';
import { IoIosAdd } from "react-icons/io";

const MOCK_MEMBERS = [
  {
    name: "Amy Brown",
    email: "am748@iu.edu"
  },
  {
    name: "Bob Smith",
    email: "bobSmith@gmail.com"
  },
  {
    name: "Charlie Green",
    email: "charlieGreen@gmail.com"
  }
]

interface GroupMembersProps {
  groupId: string | undefined;
}

export const GroupMembers = ({ groupId }: GroupMembersProps) => {
  const toast = useToast();
  if (!groupId) {
    return (
      <Text>No group selected</Text>
    )
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
          />
        </InputGroup>
        <Box>
          <ActionButton
            icon={IoIosAdd}
            onClick={() => {
              console.log('Search')
            }}>
              Add members
            </ActionButton>
        </Box>

      </Flex>

      <TableContainer mt={8}>
        <Table size='sm'>
          <Thead>
            <Tr>
              <Th>Name</Th>
              <Th>Email</Th>
              <Th>Action</Th>
            </Tr>
          </Thead>
          <Tbody>
            {
              MOCK_MEMBERS.map((member, index) => (
                <Tr key={index}>
                  <Td>{member.name}</Td>
                  <Td>{member.email}</Td>
                  <Td>
                    <Button
                      variant='link'
                      color='blue.400'
                      size='sm'
                    >
                      Remove
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
