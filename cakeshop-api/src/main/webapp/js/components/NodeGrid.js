import Grid from "@material-ui/core/Grid";
import Card from "@material-ui/core/Card";
import CardContent from "@material-ui/core/CardContent";
import Typography from "@material-ui/core/Typography";
import CardActions from "@material-ui/core/CardActions";
import Button from "@material-ui/core/Button";
import React from "react";
import {makeStyles} from "@material-ui/styles";

const useStyles = makeStyles({
    card: {
        width: 220,
    },
    cardContent: {
        width: "100%",
        minHeight: 120,
    },
});
export const NodeGrid = ({list, onView, onDismiss}) => {
    const classes = useStyles();
    return <Grid container spacing={2}>
        {list.map(node =>
            <Grid item
                  key={node.rpcUrl} zeroMinWidth>
                <Card className={classes.card}>
                    <CardContent className={classes.cardContent}>
                        <Typography variant="h6" component="h3" gutterBottom>
                            {node.name}
                        </Typography>
                        <Typography variant="subtitle2" color="textSecondary"
                                    noWrap>
                            {node.rpcUrl}
                        </Typography>
                        <Typography variant="subtitle2" color="textSecondary"
                                    noWrap>
                            {node.transactionManagerUrl}
                        </Typography>
                    </CardContent>
                    <CardActions>
                        <Button color="primary" onClick={(e) => onView(node)}
                                size="small">View</Button>
                        <Button onClick={(e) => onDismiss(node)}
                                size="small">Remove</Button>
                    </CardActions>
                </Card>
            </Grid>
        )}
    </Grid>
};
