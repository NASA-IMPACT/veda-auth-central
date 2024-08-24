export interface GroupMembership {
  id: string;
  name: string;
  created_time: string;
  last_modified_time: string;
  description: string;
  owner_id: string;
  num_members?: number;
  your_role?: string;
};
