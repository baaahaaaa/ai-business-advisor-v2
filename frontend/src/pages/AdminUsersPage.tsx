import { useEffect, useState } from "react";
import type { FormEvent } from "react";

import { useAuth } from "../auth/useAuth";
import {
  createAdminUser,
  getAdminUsers,
  setAdminUserEnabled,
  type AdminUser,
  type CreateAdminUserRequest,
} from "../api/adminApi";

import "./admin-users.css";

type RoleFilter = "ALL" | "ADMIN" | "ANALYST";
type StatusFilter = "ALL" | "ACTIVE" | "DISABLED";

function formatDate(value: string | null): string {
  if (!value) return "Never";

  const date = new Date(value);

  return Number.isNaN(date.getTime())
    ? "Unavailable"
    : date.toLocaleString("fr-FR");
}

export default function AdminUsersPage() {
  const { user, accessToken } = useAuth();
  const isAdmin = user?.role === "ADMIN";

  const [users, setUsers] = useState<AdminUser[] | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [refreshKey, setRefreshKey] = useState(0);

  const [showCreate, setShowCreate] = useState(false);
  const [creating, setCreating] = useState(false);

  const [newUser, setNewUser] = useState<CreateAdminUserRequest>({
    email: "",
    password: "",
    firstName: "",
    lastName: "",
    role: "ANALYST",
  });

  const [confirmPassword, setConfirmPassword] = useState("");
  const [createError, setCreateError] = useState<string | null>(null);
  const [createSuccess, setCreateSuccess] = useState<string | null>(null);

  const [updatingUserId, setUpdatingUserId] =
    useState<string | null>(null);

  const [statusError, setStatusError] =
    useState<string | null>(null);

  const [statusSuccess, setStatusSuccess] =
    useState<string | null>(null);

  const [search, setSearch] = useState("");
  const [roleFilter, setRoleFilter] = useState<RoleFilter>("ALL");
  const [statusFilter, setStatusFilter] =
    useState<StatusFilter>("ALL");

  useEffect(() => {
    if (!isAdmin || !accessToken) return;

    const controller = new AbortController();

    getAdminUsers(accessToken, controller.signal)
      .then((response) => {
        if (!controller.signal.aborted) {
          setUsers(response);
        }
      })
      .catch((exception: unknown) => {
        if (!controller.signal.aborted) {
          setError(
            exception instanceof Error
              ? exception.message
              : "Unable to load users."
          );
        }
      })
      .finally(() => {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      });

    return () => controller.abort();
  }, [accessToken, isAdmin, refreshKey]);

  function refreshUsers() {
    setLoading(true);
    setError(null);
    setUsers(null);
    setRefreshKey((current) => current + 1);
  }

  async function handleCreateUser(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    if (creating) return;

    setCreateError(null);
    setCreateSuccess(null);

    if (!accessToken) {
      setCreateError("Session expired. Please sign in again.");
      return;
    }

    if (newUser.password !== confirmPassword) {
      setCreateError("Passwords do not match.");
      return;
    }

    if (
      newUser.role === "ADMIN" &&
      !window.confirm(
        "Create a new administrator? This account will have full administrative access."
      )
    ) {
      return;
    }

    setCreating(true);

    try {
      const created = await createAdminUser(accessToken, newUser);

      setNewUser({
        email: "",
        password: "",
        firstName: "",
        lastName: "",
        role: "ANALYST",
      });

      setConfirmPassword("");
      setShowCreate(false);
      setCreateSuccess(`Account ${created.email} created successfully.`);

      refreshUsers();
    } catch (exception) {
      setCreateError(
        exception instanceof Error
          ? exception.message
          : "Unable to create user."
      );
    } finally {
      setCreating(false);
    }
  }
  async function handleChangeStatus(target: AdminUser) {
    if (!isAdmin || !accessToken) {
      setStatusError("Administrator session required.");
      return;
    }

    if (updatingUserId !== null || loading || creating) {
      return;
    }

    const isOwnAccount =
      target.id === user?.id ||
      target.email.toLowerCase() === user?.email.toLowerCase();

    if (target.enabled && isOwnAccount) {
      setStatusError("You cannot disable your own account.");
      return;
    }

    const action = target.enabled ? "disable" : "activate";

    const confirmed = window.confirm(
      `Are you sure you want to ${action} the account ${target.email}?`
    );

    if (!confirmed) return;

    setUpdatingUserId(target.id);
    setStatusError(null);
    setStatusSuccess(null);

    try {
      const updated = await setAdminUserEnabled(
        accessToken,
        target.id,
        !target.enabled
      );

      setUsers((current) =>
        current
          ? current.map((item) =>
              item.id === updated.id ? updated : item
            )
          : current
      );

      setStatusSuccess(
        `Account ${updated.email} ${
          updated.enabled ? "activated" : "disabled"
        } successfully.`
      );
    } catch (exception) {
      setStatusError(
        exception instanceof Error
          ? exception.message
          : "Unable to update account status."
      );
    } finally {
      setUpdatingUserId(null);
    }
  }
  const filteredUsers = (users ?? []).filter((item) => {
    const searchValue = search.trim().toLowerCase();

    const matchesSearch =
      !searchValue ||
      item.email.toLowerCase().includes(searchValue) ||
      `${item.firstName} ${item.lastName}`
        .toLowerCase()
        .includes(searchValue);

    const matchesRole =
      roleFilter === "ALL" || item.role === roleFilter;

    const matchesStatus =
      statusFilter === "ALL" ||
      (statusFilter === "ACTIVE" && item.enabled) ||
      (statusFilter === "DISABLED" && !item.enabled);

    return matchesSearch && matchesRole && matchesStatus;
  });

  const activeUsers =
    users?.filter((item) => item.enabled).length ?? 0;

  const adminUsers =
    users?.filter((item) => item.role === "ADMIN").length ?? 0;

  if (!isAdmin) {
    return (
      <main className="app-shell">
        <section className="admin-users-message">
          <h1>Access denied</h1>
          <p>Administrator access is required.</p>
        </section>
      </main>
    );
  }

  return (
    <div className="app-shell admin-users-page">
      <header className="topbar">
        <div>
          <p className="eyebrow">ADMINISTRATION WORKSPACE</p>
          <h1>User Management</h1>
          <p className="subtitle">
            View registered accounts, access roles and
            account status.
          </p>
        </div>

        <button
          type="button"
          className="secondary-button"
          onClick={refreshUsers}
          disabled={loading || updatingUserId !== null}
        >
          {loading ? "Loading..." : "Refresh users"}
        </button>
      </header>

      {!loading && !error && users && (
        <section className="admin-users-stats">
          <article>
            <span>Total users</span>
            <strong>{users.length}</strong>
          </article>

          <article>
            <span>Active accounts</span>
            <strong>{activeUsers}</strong>
          </article>

          <article>
            <span>Administrators</span>
            <strong>{adminUsers}</strong>
          </article>
        </section>
      )}

      <section className="admin-users-panel">
        <div className="admin-users-heading">
          <div>
            <p className="section-label">ACCESS CONTROL</p>
            <h2>Registered users</h2>
          </div>

          <p className="admin-users-count">
            {!loading && users
              ? `${filteredUsers.length} of ${users.length} users`
              : "User directory"}
          </p>
        </div>

        <div className="admin-users-create-toolbar">
          <button
            type="button"
            className="admin-users-add-button"
            disabled={creating}
            onClick={() => {
              setShowCreate((current) => !current);
              setCreateError(null);
            }}
          >
            {showCreate ? "Close form" : "+ Add User"}
          </button>
        </div>

        {createSuccess && (
          <div className="admin-users-create-success" role="status">
            {createSuccess}
          </div>
        )}

        {showCreate && (
          <form
            className="admin-users-create-form"
            onSubmit={handleCreateUser}
          >
            <h3>Create a new user</h3>

            <p>
              New accounts are enabled automatically.
              Select the ANALYST role unless administrative
              privileges are required.
            </p>

            <div className="admin-users-create-grid">
              <label>
                First name
                <input
                  type="text"
                  required
                  maxLength={100}
                  autoComplete="given-name"
                  value={newUser.firstName}
                  onChange={(event) =>
                    setNewUser((current) => ({
                      ...current,
                      firstName: event.target.value,
                    }))
                  }
                />
              </label>

              <label>
                Last name
                <input
                  type="text"
                  required
                  maxLength={100}
                  autoComplete="family-name"
                  value={newUser.lastName}
                  onChange={(event) =>
                    setNewUser((current) => ({
                      ...current,
                      lastName: event.target.value,
                    }))
                  }
                />
              </label>

              <label>
                Email address
                <input
                  type="email"
                  required
                  autoComplete="off"
                  value={newUser.email}
                  onChange={(event) =>
                    setNewUser((current) => ({
                      ...current,
                      email: event.target.value,
                    }))
                  }
                />
              </label>

              <label>
                Role
                <select
                  value={newUser.role}
                  onChange={(event) =>
                    setNewUser((current) => ({
                      ...current,
                      role: event.target.value as "ADMIN" | "ANALYST",
                    }))
                  }
                >
                  <option value="ANALYST">ANALYST</option>
                  <option value="ADMIN">ADMIN</option>
                </select>
              </label>

              <label>
                Password
                <input
                  type="password"
                  required
                  minLength={12}
                  maxLength={100}
                  autoComplete="new-password"
                  value={newUser.password}
                  onChange={(event) =>
                    setNewUser((current) => ({
                      ...current,
                      password: event.target.value,
                    }))
                  }
                />
              </label>

              <label>
                Confirm password
                <input
                  type="password"
                  required
                  minLength={12}
                  maxLength={100}
                  autoComplete="new-password"
                  value={confirmPassword}
                  onChange={(event) =>
                    setConfirmPassword(event.target.value)
                  }
                />
              </label>
            </div>

            {createError && (
              <div className="admin-users-create-error" role="alert">
                {createError}
              </div>
            )}

            <div className="admin-users-create-actions">
              <button type="submit" disabled={creating}>
                {creating ? "Creating..." : "Create user"}
              </button>

              <button
                type="button"
                disabled={creating}
                onClick={() => {
                  setShowCreate(false);
                  setNewUser({
                    email: "",
                    password: "",
                    firstName: "",
                    lastName: "",
                    role: "ANALYST",
                  });
                  setConfirmPassword("");
                  setCreateError(null);
                }}
              >
                Cancel
              </button>
            </div>
          </form>
        )}
        {statusError && (
          <div className="admin-users-create-error" role="alert">
            {statusError}
          </div>
        )}

        {statusSuccess && (
          <div className="admin-users-create-success" role="status">
            {statusSuccess}
          </div>
        )}
        <div className="admin-users-filters">
          <label>
            Search users
            <input
              type="search"
              placeholder="Search name or email..."
              value={search}
              onChange={(event) => setSearch(event.target.value)}
            />
          </label>

          <label>
            Role
            <select
              value={roleFilter}
              onChange={(event) =>
                setRoleFilter(event.target.value as RoleFilter)
              }
            >
              <option value="ALL">All roles</option>
              <option value="ADMIN">Administrators</option>
              <option value="ANALYST">Analysts</option>
            </select>
          </label>

          <label>
            Status
            <select
              value={statusFilter}
              onChange={(event) =>
                setStatusFilter(event.target.value as StatusFilter)
              }
            >
              <option value="ALL">All statuses</option>
              <option value="ACTIVE">Active</option>
              <option value="DISABLED">Disabled</option>
            </select>
          </label>
        </div>

        {loading && (
          <div className="admin-users-message" role="status">
            Loading registered users...
          </div>
        )}

        {!loading && error && (
          <div className="admin-users-error" role="alert">
            <strong>Unable to load users</strong>
            <p>{error}</p>

            <button type="button" onClick={refreshUsers}>
              Try again
            </button>
          </div>
        )}

        {!loading && !error && users && (
          <>
            <div className="admin-users-table-wrap">
              <table className="admin-users-table">
                <thead>
                  <tr>
                    <th>User</th>
                    <th>Role</th>
                    <th>Status</th>
                    <th>Created at</th>
                    <th>Last login</th>
                    <th>Actions</th>
                  </tr>
                </thead>

                <tbody>
                  {filteredUsers.map((item) => (
                    <tr key={item.id}>
                      <td>
                        <div className="admin-users-identity">
                          <span className="admin-users-avatar">
                            {item.firstName.charAt(0).toUpperCase()}
                            {item.lastName.charAt(0).toUpperCase()}
                          </span>

                          <div>
                            <strong>
                              {item.firstName} {item.lastName}
                            </strong>
                            <small>{item.email}</small>
                          </div>
                        </div>
                      </td>

                      <td>
                        <span
                          className={
                            item.role === "ADMIN"
                              ? "admin-users-role role-admin"
                              : "admin-users-role role-analyst"
                          }
                        >
                          {item.role}
                        </span>
                      </td>

                      <td>
                        <span
                          className={
                            item.enabled
                              ? "admin-users-status status-active"
                              : "admin-users-status status-disabled"
                          }
                        >
                          {item.enabled ? "Active" : "Disabled"}
                        </span>
                      </td>

                      <td>{formatDate(item.createdAt)}</td>
                      <td>{formatDate(item.lastLoginAt)}</td>

                      <td>
                        {item.enabled &&
                        (item.id === user?.id ||
                          item.email.toLowerCase() ===
                            user?.email.toLowerCase()) ? (
                          <span className="admin-users-self">
                            Current account
                          </span>
                        ) : (
                          <button
                            type="button"
                            className={
                              item.enabled
                                ? "admin-users-action action-disable"
                                : "admin-users-action action-activate"
                            }
                            disabled={
                              updatingUserId !== null ||
                              loading ||
                              creating
                            }
                            onClick={() => {
                              void handleChangeStatus(item);
                            }}
                            aria-label={`${
                              item.enabled ? "Disable" : "Activate"
                            } account ${item.email}`}
                          >
                            {updatingUserId === item.id
                              ? "Updating..."
                              : item.enabled
                                ? "Disable"
                                : "Activate"}
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {filteredUsers.length === 0 && (
              <div className="admin-users-message">
                No users match the selected filters.
              </div>
            )}
          </>
        )}

        <p className="admin-users-footer">
          Account changes require administrator authorization.
          Status updates must be confirmed before submission.
        </p>
      </section>
    </div>
  );
}
