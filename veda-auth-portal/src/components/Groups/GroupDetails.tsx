import React, { useState } from "react";
import { Link } from "react-router-dom";
import { NavContainer } from "../NavContainer";
import { PageTitle } from "../PageTitle";
import { FaArrowLeft } from "react-icons/fa6";
import {
  Flex,
  Icon,
  Box,
  Text,
  Tabs, TabList, TabPanels, Tab, TabPanel
} from "@chakra-ui/react";
import { useEffect } from "react";
import { GroupMembership } from "../../interfaces/GroupMembership";
import { GroupSettings } from "./GroupSettings";
import { GroupMembers } from "./GroupMembers";

const MOCK_GROUP_BASIC_INFO = {
  "id": "group_1",
  "name": "group_1",
  "created_time": "1723691939603",
  "last_modified_time": "1723691939606",
  "description": "this is a test description for the group",
  "owner_id": "ganning.xu@gatech.edu"
};

interface CustomTabProps {
  children: React.ReactNode;
}

const CustomTab = ({ children }: CustomTabProps) => {
  return (
    <Tab
      _selected={{
        borderColor: 'default.default',
      }}
    >
      {children}
    </Tab>
  );
}

export const GroupDetails = () => {
  const [basicInfo, setBasicInfo] = useState<GroupMembership | null>(null);

  useEffect(() => {
    setTimeout(() => {
      setBasicInfo(MOCK_GROUP_BASIC_INFO);
    }, 500);
  }, []);


  return (
    <>
      <NavContainer activeTab="Groups">
        <Link to="/groups">
          <Flex alignItems='center' gap={2} color="default.secondary">
            <Icon as={FaArrowLeft}  />
            <Text fontWeight='bold' fontSize='sm'>Back to Groups</Text>
          </Flex>
        </Link>

        <Box mt={4}>
          <PageTitle>{basicInfo?.name}</PageTitle>
          <Text color="default.secondary" mt={2}>{basicInfo?.description}</Text>
        </Box>

        <Tabs>
          <TabList mt={4}>
            <CustomTab>Settings</CustomTab>
            <CustomTab>Members</CustomTab>
          </TabList>

          <TabPanels>
            <TabPanel>
              <GroupSettings groupId={basicInfo?.id} />
            </TabPanel>
            <TabPanel>
              <GroupMembers groupId={basicInfo?.id} />
            </TabPanel>
          </TabPanels>
      </Tabs>


      </NavContainer>
    </>
  );
}
