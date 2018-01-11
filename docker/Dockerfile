FROM postgres:9.5.5

USER root

ENV DEBIAN_FRONTEND=noninteractive
ENV TERM=linux

# Install some useful utils.
RUN apt-get update && apt-get install -y \
  apt-utils \
  jq \
  less \
  locales \
  sudo \
  vim;

# Create Finnish and English (UTF-8) locales,
# and set the English one as the OS locale.
RUN sed -i 's/# fi_FI.UTF-8 UTF-8/fi_FI.UTF-8 UTF-8/g' /etc/locale.gen;
RUN sed -i 's/# en_US.UTF-8 UTF-8/en_US.UTF-8 UTF-8/g' /etc/locale.gen;
RUN locale-gen;
RUN dpkg-reconfigure -f noninteractive locales;
ENV LANG=en_US.UTF-8
ENV LC_ALL=en_US.UTF-8

# Set the Finnish timezone.
RUN ln -fs /usr/share/zoneinfo/Europe/Helsinki /etc/localtime;
RUN echo "Europe/Helsinki" > /etc/timezone;
RUN dpkg-reconfigure -f noninteractive tzdata;

# Create user jaanmurtaja.
RUN useradd --create-home --gid=postgres --home-dir=/home/jaanmurtaja --shell=/bin/bash --system --uid=666 jaanmurtaja;
# Set the password of user jaanmurtaja to 'm9Cf7YcNxZFrNTag'.
RUN echo "jaanmurtaja:m9Cf7YcNxZFrNTag" | chpasswd;
RUN sed -i '21ijaanmurtaja	ALL=(ALL:ALL) ALL' /etc/sudoers;

USER jaanmurtaja

ENV PGDATA=/home/jaanmurtaja/postgresql/data
ENV PAGER=less
ENV LESS='-iMSx4 -RSFX -e'

RUN rm -rf ${PGDATA}
RUN mkdir -p ${PGDATA}

WORKDIR /home/jaanmurtaja/postgresql/data

COPY init.sql /home/jaanmurtaja/init.sql

# Create database jaanmurtaja.
RUN initdb --encoding=UTF-8 --lc-collate=fi_FI.UTF-8 --lc-ctype=fi_FI.UTF-8 --lc-messages=en_US.UTF-8 --lc-monetary=fi_FI.UTF-8 --lc-numeric=fi_FI.UTF-8 --lc-time=fi_FI.UTF-8 --locale=fi_FI.UTF-8 --pgdata=${PGDATA} --username=jaanmurtaja;
RUN pg_ctl start -w -D ${PGDATA}; \
  createdb --encoding=UTF-8 --owner=jaanmurtaja jaanmurtaja; \
  psql -c "ALTER ROLE jaanmurtaja WITH PASSWORD 'argxBX4DxWJKC7st'"; \
  psql -c "CREATE ROLE tavis WITH NOSUPERUSER LOGIN ENCRYPTED PASSWORD 'Mansikka2'"; \
  psql < /home/jaanmurtaja/init.sql; \
  pg_ctl stop -w -D ${PGDATA} --mode=smart;

# Modify the client authentication file (pg_hba.conf) so that users "jaanmurtaja"
# and "tavis" can log in from outside the container.
RUN echo 'host	all	jaanmurtaja	0.0.0.0/0	md5' >> ${PGDATA}/pg_hba.conf;
RUN echo 'host	all	tavis	0.0.0.0/0	md5' >> ${PGDATA}/pg_hba.conf;

ENTRYPOINT postgres -D ${PGDATA} -h 0.0.0.0 -i -p 5432;
CMD []
