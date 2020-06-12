import React from 'react';
import Paper from '@material-ui/core/Paper';
import Table from '@material-ui/core/Table';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import TableCell from '@material-ui/core/TableCell';
import TableBody from '@material-ui/core/TableBody';
import TableContainer from '@material-ui/core/TableContainer';
import TextareaAutosize from '@material-ui/core/TextareaAutosize';
import CircularProgress from '@material-ui/core/CircularProgress';

export const Report = (props) => {
    return (
        <div>
            <TableContainer component={Paper}>
                <Table size="small" aria-label="collapsible table">
                    <TableHead>
                        <TableRow>
                            <TableCell width="20%" style={{fontSize: "14px"}}><strong>Block Number</strong></TableCell>
                            <TableCell width="80%" style={{fontSize: "14px"}}><strong>State</strong></TableCell>
                        </TableRow>
                    </TableHead>
                    { !props.isLoading &&
                        <TableBody>
                            {
                                props.parsedStorage.map((s, i) => (
                                    <TableRow key={i}>
                                        <TableCell style={{fontSize: "14px"}}>{s.blockNumber}</TableCell>
                                        <TableCell>
                                            <Table size="small" aria-label="collapsible table">
                                                <TableBody>
                                                    {
                                                        s.historicStorage.map((v, i) => (
                                                            v.changed?
                                                                <TableRow key={i} style={{backgroundColor:'#F0F0F0'}}>
                                                                    <TableCell width="20%" style={{fontSize: "14px"}}><div>{v.name}</div></TableCell>
                                                                    <TableCell width="20%" style={{fontSize: "14px"}}><div>{v.type}</div></TableCell>
                                                                    <TableCell width="60%" style={{fontSize: "14px"}}>
                                                                        <div style={{maxWidth: "300px"}}>
                                                                            {
                                                                                v.type === "string" ?
                                                                                    <TextareaAutosize
                                                                                        readOnly
                                                                                        rowsMax={4}
                                                                                        rowsMin={2}
                                                                                        aria-label="maximum height"
                                                                                        style={{fontSize: "16px", width: "300px"}}
                                                                                        defaultValue={"\""+v.value+"\""}
                                                                                    /> : v.value.toString()
                                                                            }
                                                                        </div>
                                                                    </TableCell>
                                                                </TableRow> :
                                                                <TableRow key={i}>
                                                                    <TableCell width="20%" style={{fontSize: "14px"}}><div>{v.name}</div></TableCell>
                                                                    <TableCell width="20%" style={{fontSize: "14px"}}><div>{v.type}</div></TableCell>
                                                                    <TableCell width="60%" style={{fontSize: "14px"}}>
                                                                        <div style={{maxWidth: "300px"}}>
                                                                            {
                                                                                v.type === "string" ?
                                                                                    <TextareaAutosize
                                                                                        readOnly
                                                                                        rowsMax={4}
                                                                                        rowsMin={2}
                                                                                        aria-label="maximum height"
                                                                                        style={{fontSize: "16px", width: "300px"}}
                                                                                        defaultValue={"\""+v.value+"\""}
                                                                                    /> : v.value.toString()
                                                                            }
                                                                        </div>
                                                                    </TableCell>
                                                                </TableRow>
                                                        ))
                                                    }
                                                </TableBody>
                                            </Table>
                                        </TableCell>
                                    </TableRow>
                                ))
                            }
                        </TableBody>
                    }
                </Table>
            </TableContainer>
            { props.isLoading &&
                <div align="center">
                    <br/>
                    <CircularProgress/>
                </div>
            }
        </div>
    )
}
