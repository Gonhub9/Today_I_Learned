import React, { useState, useEffect } from 'react';
import { DragDropContext } from '@hello-pangea/dnd';
import KanbanColumn from './KanbanColumn';
import * as api from '../../api';

const KanbanBoard = ({ project }) => {
    const [columns, setColumns] = useState([]);
    const [cards, setCards] = useState([]);

    useEffect(() => {
        if (project && project.mainBoardId) {
            api.getColumnsByBoard(project.mainBoardId).then(response => {
                setColumns(response.data);
            });
            api.getCardsByProject(project.id).then(response => {
                setCards(response.data);
            });
        }
    }, [project]);

    const onDragEnd = (result) => {
        const { destination, source, draggableId } = result;

        if (!destination) {
            return;
        }

        if (
            destination.droppableId === source.droppableId &&
            destination.index === source.index
        ) {
            return;
        }

        const startColumnId = parseInt(source.droppableId, 10);
        const endColumnId = parseInt(destination.droppableId, 10);
        const cardId = parseInt(draggableId, 10);

        // Optimistic UI update
        const updatedCards = cards.map(card =>
            card.id === cardId ? { ...card, kanbanColumnId: endColumnId } : card
        );
        setCards(updatedCards);

        // API call
        api.shiftCard(cardId, { 
            newColumnId: endColumnId, 
            newPosition: destination.index 
        }).catch(error => {
            console.error("Failed to move card", error);
            // Revert UI on failure
            setCards(cards);
        });
    };

    if (!project) {
        return <div>프로젝트를 선택해주세요.</div>;
    }

    return (
        <div style={{ overflowX: 'auto'}} className="flex-grow-1">
            <DragDropContext onDragEnd={onDragEnd}>
                <div style={{ display: 'flex', gap: '1rem', minHeight: '100%' }}>
                    {columns.sort((a, b) => a.position - b.position).map(column => {
                        const columnCards = cards.filter(card => card.kanbanColumnId === column.id);
                        // We need to sort cards by their position if available
                        // For now, we rely on the order from the server
                        return <KanbanColumn key={column.id} column={column} cards={columnCards} />;
                    })}
                </div>
            </DragDropContext>
        </div>
    );
};

export default KanbanBoard;
