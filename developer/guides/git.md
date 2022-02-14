# Git Developer Guide

The source code is stored on [https://github.com](GitHub.com) which uses the 
[https://git-scm.com](Git) version control system.

The purpose of this guide is to provide and overview of the common commands used for git
repositories.

# Configuration

Before using git for the first time the following configuration steps must be performed. 

## Set User Details

Git requires the user.name and user.email configuration properties to be set before commits can be
made.

To set the config properties in your user account's global config file use the following commands.

```
git config --global user.name "Mary Smith"
git config --global user.email "mary.smith@example.com"
```

To set the config properties for a single local git repository file use the following commands.

```
cd projects/Test
git config --local user.name "Mark Smith"
git config --local user.email "mark.smith@example.com"
```

For GitHub.com commits you can hide your real email address by configuring and using a 
[GitHub noreply email address](https://docs.github.com/en/account-and-profile/setting-up-and-managing-your-github-user-account/managing-email-preferences/setting-your-commit-email-address).

# Commands

## Clone

The folowing command checks out 

git clone git@github.com:revolsys/com.revolsys.open.git

## Pull

git pull

## Push

## Fetch

git fetch
