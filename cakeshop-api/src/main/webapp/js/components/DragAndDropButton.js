import React, {useCallback} from "react";
import {useDropzone} from "react-dropzone";
import Button from "@material-ui/core/Button";

export const DragAndDropButton = ({onClick, onConfigLoaded}) => {
    const onDrop = useCallback(acceptedFiles => {
        const reader = new FileReader();

        reader.onabort = () => console.log('file reading was aborted');
        reader.onerror = () => console.log('file reading has failed');
        reader.onload = () => {
            // Do whatever you want with the file contents
            const input = JSON.parse(reader.result);
            onConfigLoaded(input);
        };

        reader.readAsBinaryString(acceptedFiles[0]);
    }, []);
    const {getRootProps, getInputProps, isDragActive} = useDropzone({onDrop})

    return (
        <div {...getRootProps()}>
            <input {...getInputProps()} />
            <Button color="inherit" onClick={onClick}>
                {isDragActive ? "Drop Here" : "Add Node"}
            </Button>
        </div>
    )
};
