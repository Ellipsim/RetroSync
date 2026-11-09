
import { useEffect, useState, type ChangeEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../services/auth';
import {
  getSaves,
  uploadSave,
  downloadSave,
  deleteSave,
} from '../services/api';
import type { Save } from '../types';

function Dashboard() {
  const navigate = useNavigate();
  const { user, logout } = useAuth();

  const [saves, setSaves] = useState<Save[]>([]);
  const [loading, setLoading] = useState(true);
  const [uploading, setUploading] = useState(false);
  const [error, setError] = useState('');

  async function loadSaves() {
    if (!user) return;

    try {
      setError('');
      const data = await getSaves(user.id);
      setSaves(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to load saves');
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadSaves();
  }, [user]);

  function handleLogout() {
    logout();
    navigate('/login', { replace: true });
  }

  async function handleUpload(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file || !user) return;

    try {
      setUploading(true);
      setError('');
      await uploadSave(user.id, file);
      await loadSaves();
      event.target.value = '';
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Upload failed');
    } finally {
      setUploading(false);
    }
  }

  async function handleDownload(save: Save) {
    if (!user) return;

    try {
      setError('');
      const blob = await downloadSave(user.id, save.id);
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');

      link.href = url;
      link.download = save.fileName;
      document.body.appendChild(link);
      link.click();
      link.remove();
      URL.revokeObjectURL(url);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Download failed');
    }
  }

  async function handleDelete(save: Save) {
    if (!user) return;
    if (!window.confirm(`Delete ${save.fileName}?`)) return;

    try {
      setError('');
      await deleteSave(user.id, save.id);
      setSaves((current) =>
        current.filter((item) => item.id !== save.id)
      );
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Delete failed');
    }
  }

  function formatSize(bytes: number) {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  return (
    <main className="dashboard-page">
      <header>
        <h1>RetroSync</h1>
        <p>Logged in as: {user?.email}</p>
        <button onClick={handleLogout}>Log out</button>
      </header>

      <section>
        <h2>My Library</h2>
        <p>Keep your game saves together and access them whenever you need.</p>
        <p>{saves.length} saved files</p>
      </section>

      <section>
        <h2>Upload a save</h2>
        <p>Select a game save file from your computer.</p>

        <input
          type="file"
          onChange={handleUpload}
          disabled={uploading}
        />

        {uploading && <p>Uploading file...</p>}
      </section>

      <section>
        <h2>Your saved games</h2>

        {error && <p className="error-message" role="alert">{error}</p>}

        {loading ? (
          <p>Loading saves...</p>
        ) : saves.length === 0 ? (
          <p>You haven't uploaded any saves yet.</p>
        ) : (
          saves.map((save) => (
            <article key={save.id}>
              <h3>{save.fileName}</h3>
              <p>Size: {formatSize(save.fileSize)}</p>
              <p>
                Uploaded: {new Date(save.createdAt).toLocaleString()}
              </p>

              <button onClick={() => handleDownload(save)}>
                Download
              </button>

              <button onClick={() => handleDelete(save)}>
                Delete
              </button>
            </article>
          ))
        )}
      </section>

      <footer className="dashboard-footer">
        RetroSync — Keep your progress safe.
      </footer>
    </main>
  );
}

export default Dashboard;
