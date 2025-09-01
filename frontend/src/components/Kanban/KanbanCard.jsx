import React from 'react';
import { Draggable } from '@hello-pangea/dnd';

const KanbanCard = ({ card, index }) => {
    return (
        <Draggable draggableId={String(card.id)} index={index}>
            {(provided) => (
                <div
                    ref={provided.innerRef}
                    {...provided.draggableProps}
                    {...provided.dragHandleProps}
                    style={{
                        userSelect: 'none',
                        padding: 16,
                        margin: '0 0 8px 0',
                        minHeight: '50px',
                        backgroundColor: '#fff',
                        color: '#333',
                        border: '1px solid #ddd',
                        borderRadius: '4px',
                        ...provided.draggableProps.style,
                    }}
                >
                    <h5>{card.title}</h5>
                    <p>{card.description}</p>
                    <div style={{ display: 'flex', flexWrap: 'wrap' }}>
                        {card.tags && card.tags.map(tag => (
                            <span key={tag.id} style={{
                                backgroundColor: tag.color || '#e0e0e0',
                                color: '#333',
                                padding: '2px 8px',
                                borderRadius: '12px',
                                marginRight: '4px',
                                marginBottom: '4px',
                                fontSize: '12px'
                            }}>
                                {tag.name}
                            </span>
                        ))}
                    </div>
                </div>
            )}
        </Draggable>
    );
};

export default KanbanCard;
