import React from 'react';
import KanbanBoard from '../components/Kanban/KanbanBoard';

function DashboardPage({ selectedProject }) {
    return (
        <div className="d-flex flex-column flex-grow-1 h-100 py-3">
            <div className="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pb-2 mb-3 border-bottom">
                <h1 className="h2">{selectedProject ? selectedProject.title : '대시보드'}</h1>
            </div>

            {selectedProject ? (
                <KanbanBoard project={selectedProject} />
            ) : (
                <div className="d-flex flex-column justify-content-center align-items-center flex-grow-1">
                    <div className="text-center">
                        <h3>프로젝트를 선택하거나 새 프로젝트를 만들어주세요.</h3>
                        <p className="text-muted">왼쪽 사이드바에서 프로젝트를 관리할 수 있습니다.</p>
                    </div>
                </div>
            )}
        </div>
    );
}

export default DashboardPage;
