import React from 'react';

interface TagsInputProps {
    name?: string;
    placeHolder?: string;
    value?: string[];
    onChange?: (tags: string[]) => void;
    onBlur?: any;
    separators?: string[];
    disableBackspaceRemove?: boolean;
    onExisting?: (tag: string) => void;
    onRemoved?: (tag: string) => void;
    disabled?: boolean;
    isEditOnRemove?: boolean;
    beforeAddValidate?: (tag: string, existingTags: string[]) => boolean;
    onKeyUp?: (e: React.KeyboardEvent<HTMLInputElement>) => void;
    classNames?: {
        input?: string;
        tag?: string;
    };
}
declare const TagsInput: ({ name, placeHolder, value, onChange, onBlur, separators, disableBackspaceRemove, onExisting, onRemoved, disabled, isEditOnRemove, beforeAddValidate, onKeyUp, classNames, }: TagsInputProps) => JSX.Element;

export { TagsInput, TagsInputProps };
