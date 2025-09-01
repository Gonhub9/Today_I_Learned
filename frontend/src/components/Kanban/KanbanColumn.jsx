import React from 'react';
import { Droppable } from '@hello-pangea/dnd';
import KanbanCard from './KanbanCard';

const KanbanColumn = ({ column, cards }) => {
    return (
        <div style={{
            margin: '8px',
            border: '1px solid lightgrey',
            borderRadius: '2px',
            width: '280px',
            display: 'flex',
            flexDirection: 'column',
            backgroundColor: '#f4f5f7'
        }}>
            <h3 style={{ padding: '16px' }}>{column.title}</h3>
            <Droppable droppableId={String(column.id)} type="card">
                {(provided, snapshot) => (
                    <div
                        ref={provided.innerRef}
                        {...provided.droppableProps}
                        style={{
                            padding: '8px',
                            transition: 'background-color 0.2s ease',
                            backgroundColor: snapshot.isDraggingOver ? 'lightblue' : 'inherit',
                            flexGrow: 1,
                            minHeight: '100px'
                        }}
                    >
                        {cards.map((card, index) => (
                            <KanbanCard key={card.id} card={card} index={index} />
                        ))}
                        {provided.placeholder}
                    </div>
                )}
            </Droppable>
        </div>
    );
};

export default KanbanColumn;
