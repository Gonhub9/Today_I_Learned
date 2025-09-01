import React, { useState, useEffect } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import Layout from './components/Layout';
import LoginPage from './pages/LoginPage';
import SignupPage from './pages/SignupPage';
import DashboardPage from './pages/DashboardPage';
import * as api from './api';

import './App.css';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [projects, setProjects] = useState([]);
  const [selectedProject, setSelectedProject] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem('accessToken');
    if (token) {
      setIsAuthenticated(true);
    } else {
      setIsAuthenticated(false);
    }
  }, []);

  useEffect(() => {
    if (isAuthenticated) {
      fetchProjects();
    }
  }, [isAuthenticated]);

  const fetchProjects = () => {
    api.getProjects()
      .then(response => {
        setProjects(response.data);
        if (response.data.length > 0 && (!selectedProject || !response.data.find(p => p.id === selectedProject.id))) {
          handleSelectProject(response.data[0].id);
        } else if (response.data.length === 0) {
          setSelectedProject(null);
        }
      })
      .catch(error => {
        console.error("Error fetching projects:", error);
      });
  };

  const handleSelectProject = (projectId) => {
    if (projectId) {
      api.getProjectById(projectId).then(response => {
        setSelectedProject(response.data);
      });
    } else {
      setSelectedProject(null);
    }
  };

  const handleCreateProject = async (projectTitle) => {
    try {
      const response = await api.createProject({ 
        title: projectTitle, 
        description: '', 
        category: 'Default' 
      });
      const newProject = response.data;
      setProjects(prevProjects => [...prevProjects, newProject]);
      handleSelectProject(newProject.id);
    } catch (error) {
      console.error("Error creating project:", error);
    }
  };

  const handleLogin = () => {
      const token = localStorage.getItem('accessToken');
      if (token) {
          setIsAuthenticated(true);
      }
  }

  return (
    <Routes>
      <Route path="/login" element={<LoginPage onLoginSuccess={handleLogin} />} />
      <Route path="/signup" element={<SignupPage />} />

      <Route
        path="/"
        element={
          isAuthenticated ? (
            <Layout
              projects={projects}
              selectedProject={selectedProject}
              onSelectProject={handleSelectProject}
              onCreateProject={handleCreateProject}
            />
          ) : (
            <Navigate to="/login" replace />
          )
        }
      >
        <Route index element={<DashboardPage selectedProject={selectedProject} />} />
      </Route>

      <Route path="*" element={<Navigate to={isAuthenticated ? "/" : "/login"} replace />} />
    </Routes>
  );
}

export default App;
