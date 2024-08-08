import { Table, TableContainer, Tbody, Td, Th, Thead, Tr } from '@chakra-ui/react';

type Props = {
  claims: { [id: string]: any }
};

function TokenClaimsTable({ claims }: Props) {
  return (
    <TableContainer>
      <Table variant='simple'>
        <Thead>
          <Tr>
            <Th>項目</Th>
            <Th>値</Th>
          </Tr>
        </Thead>
        <Tbody>
          {
            Object.keys(claims).map((key) => {
              return (
                <Tr key={key}>
                  <Td>{key}</Td>
                  <Td>{String(claims[key])}</Td>
                </Tr>
              )
            })
          }
        </Tbody>
      </Table>
    </TableContainer>
  )
}

export default TokenClaimsTable;
