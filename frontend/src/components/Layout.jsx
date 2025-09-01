import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';

// A simple modal component
const CreateProjectModal = ({ isOpen, onClose, onCreate }) => {
    const [projectName, setProjectName] = useState('');

    if (!isOpen) return null;

    const handleSubmit = (e) => {
        e.preventDefault();
        if (projectName.trim()) {
            onCreate(projectName.trim());
            setProjectName('');
            onClose();
        }
    };

    return (
        <div className="modal show" style={{ display: 'block', backgroundColor: 'rgba(0,0,0,0.5)' }} tabIndex="-1">
            <div className="modal-dialog modal-dialog-centered">
                <div className="modal-content">
                    <div className="modal-header">
                        <h5 className="modal-title">새 프로젝트 생성</h5>
                        <button type="button" className="btn-close" onClick={onClose}></button>
                    </div>
                    <div className="modal-body">
                        <form onSubmit={handleSubmit}>
                            <div className="mb-3">
                                <label htmlFor="projectName" className="form-label">프로젝트 이름</label>
                                <input
                                    type="text"
                                    className="form-control"
                                    id="projectName"
                                    value={projectName}
                                    onChange={(e) => setProjectName(e.target.value)}
                                    autoFocus
                                />
                            </div>
                        </form>
                    </div>
                    <div className="modal-footer">
                        <button type="button" className="btn btn-secondary" onClick={onClose}>취소</button>
                        <button type="button" className="btn btn-primary" onClick={handleSubmit}>생성</button>
                    </div>
                </div>
            </div>
        </div>
    );
};


function Layout({ projects, selectedProject, onSelectProject, onCreateProject }) {
    const [isModalOpen, setIsModalOpen] = useState(false);

    return (
        <>
            <div className="container-fluid vh-100">
                <div className="row h-100">
                    <nav className="col-md-3 col-lg-2 d-md-block bg-light sidebar collapse p-3 border-end">
                        <div className="position-sticky pt-3">
                            <h5 className="mb-3">Projects</h5>
                            <ul className="nav flex-column mb-3">
                                {projects.map(project => (
                                    <li key={project.id} className="nav-item">
                                        <a
                                            className={`nav-link ${selectedProject && selectedProject.id === project.id ? 'active fw-bold' : ''}`}
                                            href="#"
                                            onClick={(e) => {
                                                e.preventDefault();
                                                onSelectProject(project.id);
                                            }}
                                        >
                                            {project.title}
                                        </a>
                                    </li>
                                ))}
                            </ul>
                            <button className="btn btn-primary w-100" onClick={() => setIsModalOpen(true)}>
                                + 새 프로젝트 생성
                            </button>
                        </div>
                    </nav>

                    <main className="col-md-9 ms-sm-auto col-lg-10 px-md-4 d-flex flex-column">
                        <Outlet />
                    </main>
                </div>
            </div>
            <CreateProjectModal
                isOpen={isModalOpen}
                onClose={() => setIsModalOpen(false)}
                onCreate={onCreateProject}
            />
        </>
    );
}

export default Layout;

